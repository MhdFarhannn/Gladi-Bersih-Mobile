package com.contoh.rentalhp.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.KategoriResponse
import com.contoh.rentalhp.databinding.ActivityAlatFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.PhoneImageLoader
import com.contoh.rentalhp.ui.common.UserMessageException
import com.contoh.rentalhp.util.PhotoUploadUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.math.BigDecimal

class AlatFormActivity : BaseActivity() {
    private lateinit var binding: ActivityAlatFormBinding
    private var alatId = 0L
    private var selectedCategoryId = 0L
    private var selectedCategoryName = ""
    private var categoryOptions = emptyList<KategoriResponse>()
    private var searchJob: Job? = null
    private var selectedImageUri: Uri? = null
    private var submitting = false
    private var imagePicking = false

    private val imagePicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some picker providers only grant access for the current process.
            }
            selectImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlatFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alatId = intent.getLongExtra(EXTRA_ID, 0L)
        selectedImageUri = savedInstanceState?.getString(STATE_IMAGE_URI)?.let(Uri::parse)
        binding.toolbar.title = if (alatId > 0) getString(R.string.edit_phone_model) else getString(R.string.add_phone_model)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonRetry.setOnClickListener { loadInitialData() }
        binding.buttonSave.setOnClickListener { save() }
        binding.buttonPickImage.setOnClickListener { openGallery() }
        binding.cardImagePicker.setOnClickListener { openGallery() }
        setupCategorySearch()
        loadInitialData()
    }

    private fun setupCategorySearch() {
        binding.inputCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            categoryOptions.firstOrNull { it.nama == selectedName }?.let {
                selectedCategoryId = it.id
                selectedCategoryName = it.nama
                binding.layoutCategory.error = null
            }
        }
        binding.inputCategory.doAfterTextChanged { editable ->
            val query = editable?.toString()?.trim().orEmpty()
            if (query == selectedCategoryName) return@doAfterTextChanged
            selectedCategoryId = 0L
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                try {
                    loadCategoryOptionsInternal(query.takeIf { it.isNotBlank() })
                    binding.inputCategory.showDropDown()
                } catch (error: Throwable) {
                    Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
                }
            }
        }
        binding.inputCategory.setOnClickListener {
            if (categoryOptions.isEmpty()) loadCategoryOptions(null)
            else binding.inputCategory.showDropDown()
        }
    }

    private fun loadInitialData() {
        showLoading()
        lifecycleScope.launch {
            try {
                loadCategoryOptionsInternal(null)
                if (alatId > 0) {
                    val response = RetrofitClient.apiService.getAlatDetail(alatId)
                    val item = response.data ?: error(response.message)
                    val categoryResponse = RetrofitClient.apiService.getKategoriDetail(item.idKategori)
                    val category = categoryResponse.data ?: error(categoryResponse.message)
                    selectedCategoryId = category.id
                    selectedCategoryName = category.nama
                    binding.inputCategory.setText(category.nama, false)
                    binding.inputName.setText(item.nama)
                    binding.inputDescription.setText(item.deskripsi.orEmpty())
                    binding.inputPrice.setText(item.hargaSewa.stripTrailingZeros().toPlainString().replace('.', ','))
                    if (selectedImageUri == null) {
                        PhoneImageLoader.load(binding.imagePreview, item.imagePath, item.nama)
                    }
                }
                selectedImageUri?.let(::showSelectedImage)
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun loadCategoryOptions(search: String?) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                loadCategoryOptionsInternal(search)
                binding.inputCategory.showDropDown()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun loadCategoryOptionsInternal(search: String?) {
        val response = RetrofitClient.apiService.getKategori(1, REFERENCE_PAGE_SIZE, search)
        categoryOptions = (response.data ?: error(response.message)).items
        binding.inputCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryOptions.map { it.nama })
        )
    }

    private fun save() {
        if (submitting || imagePicking) return
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val description = binding.inputDescription.text?.toString()?.trim()?.ifBlank { null }
        val priceText = binding.inputPrice.text?.toString()?.trim().orEmpty()
        val price = priceText.replace(',', '.').toBigDecimalOrNull()
        binding.layoutCategory.error = if (selectedCategoryId < 1) getString(R.string.select_valid_category) else null
        binding.layoutName.error = when {
            name.isBlank() -> getString(R.string.required_field)
            name.length > 150 -> getString(R.string.maximum_name_150)
            else -> null
        }
        binding.layoutPrice.error = when {
            price == null -> getString(R.string.invalid_price)
            price < BigDecimal.ZERO -> getString(R.string.invalid_price)
            price > BigDecimal("9999999999999.99") -> getString(R.string.invalid_price)
            else -> null
        }
        if (binding.layoutCategory.error != null || binding.layoutName.error != null ||
            binding.layoutPrice.error != null
        ) return

        val textType = "text/plain".toMediaType()
        val categoryBody = selectedCategoryId.toString().toRequestBody(textType)
        val nameBody = name.toRequestBody(textType)
        val descriptionBody = description.orEmpty().toRequestBody(textType)
        val priceBody = requireNotNull(price).toPlainString().toRequestBody(textType)
        setSubmitting(true)
        lifecycleScope.launch {
            var imageFile: File? = null
            try {
                val imagePart = selectedImageUri?.let { uri ->
                    val file = PhotoUploadUtil.prepare(this@AlatFormActivity, uri)
                    imageFile = file
                    MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                }
                val response = if (alatId > 0) {
                    RetrofitClient.apiService.updateAlat(alatId, categoryBody, nameBody, descriptionBody, priceBody, imagePart)
                } else {
                    RetrofitClient.apiService.createAlat(categoryBody, nameBody, descriptionBody, priceBody, imagePart)
                }
                if (response.data == null) throw UserMessageException(response.message)
                setResult(RESULT_OK)
                finish()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                withContext(NonCancellable + Dispatchers.IO) { imageFile?.delete() }
                if (!isFinishing) setSubmitting(false)
            }
        }
    }

    private fun openGallery() {
        if (submitting || imagePicking) return
        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun selectImage(uri: Uri) {
        imagePicking = true
        updateImageControls()
        binding.textImageError.visibility = View.GONE
        lifecycleScope.launch {
            try {
                PhotoUploadUtil.validate(this@AlatFormActivity, uri)
                selectedImageUri = uri
                showSelectedImage(uri)
            } catch (error: Throwable) {
                val message = ApiErrorHelper.getMessage(error)
                binding.textImageError.text = message
                binding.textImageError.visibility = View.VISIBLE
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            } finally {
                imagePicking = false
                updateImageControls()
            }
        }
    }

    private fun showSelectedImage(uri: Uri) {
        Glide.with(binding.imagePreview)
            .load(uri)
            .placeholder(R.drawable.ic_phone_placeholder)
            .error(R.drawable.ic_phone_image_error)
            .fitCenter()
            .into(binding.imagePreview)
        binding.textImageMessage.setText(R.string.gallery_image_selected)
    }

    private fun updateImageControls() {
        binding.buttonSave.isEnabled = !submitting && !imagePicking
        binding.buttonPickImage.isEnabled = !submitting && !imagePicking
        binding.cardImagePicker.isEnabled = !submitting && !imagePicking
        binding.progressImage.visibility = if (imagePicking) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_IMAGE_URI, selectedImageUri?.toString())
        super.onSaveInstanceState(outState)
    }

    private fun showLoading() {
        binding.progress.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    private fun setSubmitting(submitting: Boolean) {
        this.submitting = submitting
        updateImageControls()
        binding.inputCategory.isEnabled = !submitting
        binding.inputName.isEnabled = !submitting
        binding.inputDescription.isEnabled = !submitting
        binding.inputPrice.isEnabled = !submitting
        binding.buttonSave.setText(if (submitting) R.string.saving else R.string.save)
        binding.progressSave.visibility = if (submitting) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_ID = "extra_id"
        private const val STATE_IMAGE_URI = "selected_image_uri"
        private const val REFERENCE_PAGE_SIZE = 20

        fun createIntent(context: Context, id: Long = 0L): Intent {
            return Intent(context, AlatFormActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

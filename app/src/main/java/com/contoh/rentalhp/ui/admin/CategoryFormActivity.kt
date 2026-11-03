package com.contoh.rentalhp.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.KategoriRequest
import com.contoh.rentalhp.databinding.ActivityCategoryFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CategoryFormActivity : BaseActivity() {
    private lateinit var binding: ActivityCategoryFormBinding
    private var categoryId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        categoryId = intent.getLongExtra(EXTRA_ID, 0L)
        binding.toolbar.title = if (categoryId > 0) getString(R.string.edit_category) else getString(R.string.add_category)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonRetry.setOnClickListener { loadCategory() }
        binding.buttonSave.setOnClickListener { save() }
        if (categoryId > 0) loadCategory() else showContent()
    }

    private fun loadCategory() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getKategoriDetail(categoryId)
                binding.inputName.setText((response.data ?: error(response.message)).nama)
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun save() {
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        binding.layoutName.error = if (name.isBlank()) getString(R.string.required_field) else null
        if (name.isBlank()) return
        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = if (categoryId > 0) {
                    RetrofitClient.apiService.updateKategori(categoryId, KategoriRequest(name))
                } else {
                    RetrofitClient.apiService.createKategori(KategoriRequest(name))
                }
                if (response.data == null) error(response.message)
                setResult(RESULT_OK)
                finish()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                if (!isFinishing) setSubmitting(false)
            }
        }
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
        binding.buttonSave.isEnabled = !submitting
        binding.progressSave.visibility = if (submitting) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun createIntent(context: Context, id: Long = 0L): Intent {
            return Intent(context, CategoryFormActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

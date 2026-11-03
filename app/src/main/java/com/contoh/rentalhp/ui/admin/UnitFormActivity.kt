package com.contoh.rentalhp.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.data.model.UnitAlatCreateRequest
import com.contoh.rentalhp.data.model.UnitAlatUpdateRequest
import com.contoh.rentalhp.databinding.ActivityUnitFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.util.StatusUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UnitFormActivity : BaseActivity() {
    private lateinit var binding: ActivityUnitFormBinding
    private var unitId = 0L
    private var selectedModelId = 0L
    private var selectedModelName = ""
    private var modelOptions = emptyList<AlatResponse>()
    private var selectedAvailability = "tersedia"
    private var selectedCondition = "baik"
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        unitId = intent.getLongExtra(EXTRA_ID, 0L)
        binding.toolbar.title = if (unitId > 0) getString(R.string.edit_phone_unit) else getString(R.string.add_phone_unit)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupDropdowns()
        setupModelSearch()
        binding.buttonRetry.setOnClickListener { loadInitialData() }
        binding.buttonSave.setOnClickListener { save() }
        loadInitialData()
    }

    private fun setupDropdowns() {
        val availabilityLabels = resources.getStringArray(R.array.availability_labels)
        val availabilityValues = resources.getStringArray(R.array.availability_values)
        binding.inputAvailability.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availabilityLabels)
        )
        binding.inputAvailability.setText(availabilityLabels.first(), false)
        binding.inputAvailability.setOnItemClickListener { _, _, position, _ ->
            selectedAvailability = availabilityValues[position]
        }

        val conditionLabels = resources.getStringArray(R.array.item_condition_labels)
        val conditionValues = resources.getStringArray(R.array.item_condition_values)
        binding.inputCondition.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, conditionLabels)
        )
        binding.inputCondition.setText(conditionLabels.first(), false)
        binding.inputCondition.setOnItemClickListener { _, _, position, _ ->
            selectedCondition = conditionValues[position]
        }
    }

    private fun setupModelSearch() {
        binding.inputModel.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            modelOptions.firstOrNull { it.nama == selectedName }?.let {
                selectedModelId = it.id
                selectedModelName = it.nama
                binding.layoutModel.error = null
            }
        }
        binding.inputModel.doAfterTextChanged { editable ->
            val query = editable?.toString()?.trim().orEmpty()
            if (query == selectedModelName) return@doAfterTextChanged
            selectedModelId = 0L
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                try {
                    loadModelOptionsInternal(query.takeIf { it.isNotBlank() })
                    binding.inputModel.showDropDown()
                } catch (error: Throwable) {
                    Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
                }
            }
        }
        binding.inputModel.setOnClickListener {
            if (modelOptions.isEmpty()) loadModelOptions(null) else binding.inputModel.showDropDown()
        }
    }

    private fun loadInitialData() {
        showLoading()
        lifecycleScope.launch {
            try {
                loadModelOptionsInternal(null)
                if (unitId > 0) {
                    val response = RetrofitClient.apiService.getUnitAlatDetail(unitId)
                    val unit = response.data ?: error(response.message)
                    val modelResponse = RetrofitClient.apiService.getAlatDetail(unit.idAlat)
                    val model = modelResponse.data ?: error(modelResponse.message)
                    selectedModelId = model.id
                    selectedModelName = model.nama
                    binding.inputModel.setText(model.nama, false)
                    binding.inputInventoryCode.setText(unit.kodeInventaris)
                    selectedAvailability = unit.statusKetersediaan
                    selectedCondition = unit.statusBarang
                    setDropdownValues()
                }
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun setDropdownValues() {
        val availabilityValues = resources.getStringArray(R.array.availability_values)
        val availabilityLabels = resources.getStringArray(R.array.availability_labels)
        val availabilityIndex = availabilityValues.indexOf(selectedAvailability)
        val availabilityLabel = if (availabilityIndex >= 0) {
            availabilityLabels[availabilityIndex]
        } else {
            StatusUtil.label(this, selectedAvailability)
        }
        binding.inputAvailability.setText(availabilityLabel, false)
        val conditionValues = resources.getStringArray(R.array.item_condition_values)
        val conditionLabels = resources.getStringArray(R.array.item_condition_labels)
        val conditionIndex = conditionValues.indexOf(selectedCondition).coerceAtLeast(0)
        binding.inputCondition.setText(conditionLabels[conditionIndex], false)
    }

    private fun loadModelOptions(search: String?) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                loadModelOptionsInternal(search)
                binding.inputModel.showDropDown()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun loadModelOptionsInternal(search: String?) {
        val response = RetrofitClient.apiService.getAlat(1, REFERENCE_PAGE_SIZE, search)
        modelOptions = (response.data ?: error(response.message)).items
        binding.inputModel.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modelOptions.map { it.nama })
        )
    }

    private fun save() {
        val inventoryCode = binding.inputInventoryCode.text?.toString()?.trim().orEmpty()
        binding.layoutModel.error = if (selectedModelId < 1) getString(R.string.select_valid_model) else null
        binding.layoutInventoryCode.error = if (inventoryCode.isBlank()) getString(R.string.required_field) else null
        if (binding.layoutModel.error != null || binding.layoutInventoryCode.error != null) return

        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = if (unitId > 0) {
                    RetrofitClient.apiService.updateUnitAlat(
                        unitId,
                        UnitAlatUpdateRequest(
                            selectedModelId,
                            inventoryCode,
                            selectedAvailability,
                            selectedCondition
                        )
                    )
                } else {
                    RetrofitClient.apiService.createUnitAlat(
                        UnitAlatCreateRequest(
                            selectedModelId,
                            inventoryCode,
                            selectedAvailability,
                            selectedCondition
                        )
                    )
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
        private const val REFERENCE_PAGE_SIZE = 20

        fun createIntent(context: Context, id: Long = 0L): Intent {
            return Intent(context, UnitFormActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

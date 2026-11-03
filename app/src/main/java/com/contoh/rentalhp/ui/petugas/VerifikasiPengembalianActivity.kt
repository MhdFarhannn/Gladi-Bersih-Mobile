package com.contoh.rentalhp.ui.petugas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.VerifikasiPengembalianRequest
import com.contoh.rentalhp.databinding.ActivityPengembalianFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.AccessibilityUtil
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.DatePickerHelper
import com.contoh.rentalhp.ui.peminjam.ReturnUnitAdapter
import com.contoh.rentalhp.ui.peminjam.ReturnUnitInput
import com.contoh.rentalhp.util.FormatUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate

class VerifikasiPengembalianActivity : BaseActivity() {
    private lateinit var binding: ActivityPengembalianFormBinding
    private val unitAdapter = ReturnUnitAdapter()
    private var returnId = 0L
    private var returnDate = ""
    private var rentalStartDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengembalianFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        returnId = intent.getLongExtra(EXTRA_ID, 0L)
        if (returnId < 1) {
            finish()
            return
        }
        binding.toolbar.setTitle(R.string.verify_return)
        binding.textFormDescription.setText(R.string.verification_description)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonSubmit.setText(R.string.save_verification)
        binding.recyclerUnits.apply {
            layoutManager = LinearLayoutManager(this@VerifikasiPengembalianActivity)
            adapter = unitAdapter
            isNestedScrollingEnabled = false
        }
        binding.inputReturnDate.setOnClickListener {
            DatePickerHelper.show(
                supportFragmentManager,
                getString(R.string.return_date),
                returnDate,
                maxToday = true
            ) {
                returnDate = it
                binding.inputReturnDate.setText(FormatUtil.formatDate(it))
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputReturnDate)
        binding.buttonRetry.setOnClickListener { loadReturn() }
        binding.buttonSubmit.setOnClickListener { verify() }
        loadReturn()
    }

    private fun loadReturn() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPengembalianDetail(returnId)
                val item = response.data ?: error(response.message)
                returnDate = item.tanggalKembali
                rentalStartDate = item.tanggalPeminjaman
                binding.textRental.text = getString(R.string.return_number, item.id)
                binding.inputReturnDate.setText(FormatUtil.formatDate(returnDate))
                binding.inputNote.setText(item.catatan.orEmpty())
                unitAdapter.submitItems(item.items.map {
                    ReturnUnitInput(
                        idUnitAlat = it.idUnitAlat,
                        kodeInventaris = it.kodeInventaris,
                        namaAlat = it.namaAlat,
                        statusKondisi = it.statusKondisi,
                        catatan = it.catatan.orEmpty()
                    )
                })
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun verify() {
        val invalidDate = runCatching {
            val selected = LocalDate.parse(returnDate)
            selected.isBefore(LocalDate.parse(rentalStartDate)) || selected.isAfter(LocalDate.now())
        }.getOrDefault(true)
        binding.layoutReturnDate.error = if (invalidDate) getString(R.string.invalid_return_date) else null
        if (invalidDate) return
        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.verifyPengembalian(
                    returnId,
                    VerifikasiPengembalianRequest(
                        tanggalKembali = returnDate,
                        catatan = binding.inputNote.text?.toString()?.trim()?.ifBlank { null },
                        items = unitAdapter.requests()
                    )
                )
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
        binding.buttonSubmit.isEnabled = !submitting
        binding.progressSubmit.visibility = if (submitting) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun createIntent(context: Context, id: Long): Intent {
            return Intent(context, VerifikasiPengembalianActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

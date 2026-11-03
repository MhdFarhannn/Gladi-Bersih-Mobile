package com.contoh.rentalhp.ui.peminjam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PengembalianRequest
import com.contoh.rentalhp.databinding.ActivityPengembalianFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.AccessibilityUtil
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.DatePickerHelper
import com.contoh.rentalhp.ui.common.UserMessageException
import com.contoh.rentalhp.util.FormatUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate

class PengembalianFormActivity : BaseActivity() {
    private lateinit var binding: ActivityPengembalianFormBinding
    private val unitAdapter = ReturnUnitAdapter()
    private var rentalId = 0L
    private var returnDate = FormatUtil.todayApiDate()
    private var rentalStartDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengembalianFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rentalId = intent.getLongExtra(EXTRA_RENTAL_ID, 0L)
        if (rentalId < 1) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerUnits.apply {
            layoutManager = LinearLayoutManager(this@PengembalianFormActivity)
            adapter = unitAdapter
            isNestedScrollingEnabled = false
        }
        binding.inputReturnDate.setText(FormatUtil.formatDate(returnDate))
        binding.inputReturnDate.setOnClickListener {
            DatePickerHelper.show(
                supportFragmentManager,
                getString(R.string.return_date),
                returnDate,
                maxToday = true
            ) {
                returnDate = it
                binding.inputReturnDate.setText(FormatUtil.formatDate(it))
                binding.layoutReturnDate.error = null
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputReturnDate)
        binding.buttonRetry.setOnClickListener { loadRental() }
        binding.buttonSubmit.setOnClickListener { submitReturn() }
        loadRental()
    }

    private fun loadRental() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPeminjamanDetail(rentalId)
                val rental = response.data ?: error(response.message)
                if (rental.status != "dipinjam") throw UserMessageException(getString(R.string.return_status_invalid))
                rentalStartDate = rental.tanggalPeminjaman
                val units = rental.items.flatMap { item ->
                    item.unit.map { unit ->
                        ReturnUnitInput(
                            idUnitAlat = unit.idUnitAlat,
                            kodeInventaris = unit.kodeInventaris,
                            namaAlat = item.namaAlat
                        )
                    }
                }
                if (units.isEmpty()) throw UserMessageException(getString(R.string.no_allocated_units))
                unitAdapter.submitItems(units)
                binding.textRental.text = getString(R.string.rental_number, rental.id)
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun submitReturn() {
        val invalidDate = runCatching {
            val selected = LocalDate.parse(returnDate)
            selected.isBefore(LocalDate.parse(rentalStartDate)) || selected.isAfter(LocalDate.now())
        }.getOrDefault(true)
        binding.layoutReturnDate.error = if (invalidDate) getString(R.string.invalid_return_date) else null
        if (invalidDate) return

        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPengembalian(
                    PengembalianRequest(
                        idPeminjaman = rentalId,
                        tanggalKembali = returnDate,
                        catatan = binding.inputNote.text?.toString()?.trim()?.ifBlank { null },
                        items = unitAdapter.requests()
                    )
                )
                val returnData = response.data ?: error(response.message)
                setResult(RESULT_OK)
                startActivity(PengembalianDetailActivity.createIntent(this@PengembalianFormActivity, returnData.id))
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
        private const val EXTRA_RENTAL_ID = "extra_rental_id"

        fun createIntent(context: Context, rentalId: Long): Intent {
            return Intent(context, PengembalianFormActivity::class.java)
                .putExtra(EXTRA_RENTAL_ID, rentalId)
        }
    }
}

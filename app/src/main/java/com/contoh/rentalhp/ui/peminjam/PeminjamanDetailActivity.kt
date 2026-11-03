package com.contoh.rentalhp.ui.peminjam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PeminjamanDetailResponse
import com.contoh.rentalhp.data.model.PenolakanPeminjamanRequest
import com.contoh.rentalhp.databinding.ActivityPeminjamanDetailBinding
import com.contoh.rentalhp.databinding.DialogRejectBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.DialogHelper
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.StatusUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PeminjamanDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityPeminjamanDetailBinding
    private val itemAdapter = RentalDetailItemAdapter()
    private var rentalId = 0L

    private val actionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) setResult(RESULT_OK)
        loadDetail()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeminjamanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rentalId = intent.getLongExtra(EXTRA_ID, 0L)
        if (rentalId < 1) {
            finish()
            return
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerItems.apply {
            layoutManager = LinearLayoutManager(this@PeminjamanDetailActivity)
            adapter = itemAdapter
            isNestedScrollingEnabled = false
        }
        binding.buttonRetry.setOnClickListener { loadDetail() }
        loadDetail()
    }

    private fun loadDetail() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPeminjamanDetail(rentalId)
                showContent(response.data ?: error(response.message))
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun showContent(item: PeminjamanDetailResponse) {
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
        binding.textId.text = getString(R.string.rental_number, item.id)
        binding.textBorrower.text = getString(R.string.borrower_value, item.namaPeminjam)
        binding.textDates.text = getString(
            R.string.rental_dates_value,
            FormatUtil.formatDate(item.tanggalPeminjaman),
            FormatUtil.formatDate(item.tanggalRencanaKembali)
        )
        binding.textOfficer.text = getString(
            R.string.officer_value,
            item.namaPetugas ?: getString(R.string.not_assigned)
        )
        binding.textTotal.text = getString(R.string.total_value, FormatUtil.formatRupiah(item.totalBiaya))
        StatusUtil.apply(binding.textStatus, item.status)
        itemAdapter.submitList(item.items)
        configureActions(item)
    }

    private fun configureActions(item: PeminjamanDetailResponse) {
        binding.buttonPrimary.visibility = View.GONE
        binding.buttonSecondary.visibility = View.GONE
        binding.buttonPrimary.isEnabled = true
        binding.buttonSecondary.isEnabled = true
        when (SessionManager.getInstance(this).role) {
            SessionManager.ROLE_PEMINJAM -> when (item.status) {
                "diajukan" -> {
                    binding.buttonPrimary.visibility = View.VISIBLE
                    binding.buttonPrimary.setText(R.string.edit_application)
                    binding.buttonPrimary.setOnClickListener {
                        actionLauncher.launch(PeminjamanFormActivity.createEditIntent(this, item.id))
                    }
                }
                "dipinjam" -> {
                    binding.buttonPrimary.visibility = View.VISIBLE
                    binding.buttonPrimary.setText(R.string.record_return)
                    binding.buttonPrimary.setOnClickListener {
                        actionLauncher.launch(PengembalianFormActivity.createIntent(this, item.id))
                    }
                }
            }
            SessionManager.ROLE_PETUGAS -> configureStaffActions(item)
            SessionManager.ROLE_ADMIN -> configureAdminActions(item)
        }
    }

    private fun configureAdminActions(item: PeminjamanDetailResponse) {
        if (item.status == "diajukan") {
            binding.buttonPrimary.visibility = View.VISIBLE
            binding.buttonPrimary.setText(R.string.edit_application)
            binding.buttonPrimary.setOnClickListener {
                actionLauncher.launch(PeminjamanFormActivity.createEditIntent(this, item.id))
            }
        }
        if (item.status == "selesai" || item.status == "ditolak") {
            binding.buttonSecondary.visibility = View.VISIBLE
            binding.buttonSecondary.setText(R.string.delete)
            binding.buttonSecondary.setOnClickListener {
                DialogHelper.confirm(
                    this,
                    getString(R.string.delete_rental),
                    getString(R.string.delete_rental_confirmation),
                    getString(R.string.delete)
                ) { deleteRental(item.id) }
            }
        }
    }

    private fun deleteRental(id: Long) {
        binding.buttonPrimary.isEnabled = false
        binding.buttonSecondary.isEnabled = false
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePeminjaman(id)
                Snackbar.make(binding.root, response.message, Snackbar.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } catch (error: Throwable) {
                binding.buttonPrimary.isEnabled = true
                binding.buttonSecondary.isEnabled = true
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun configureStaffActions(item: PeminjamanDetailResponse) {
        when (item.status) {
            "diajukan" -> {
                binding.buttonPrimary.visibility = View.VISIBLE
                binding.buttonSecondary.visibility = View.VISIBLE
                binding.buttonPrimary.setText(R.string.approve)
                binding.buttonSecondary.setText(R.string.reject)
                binding.buttonPrimary.setOnClickListener {
                    DialogHelper.confirm(
                        this,
                        getString(R.string.approve_rental),
                        getString(R.string.approve_rental_confirmation),
                        getString(R.string.approve)
                    ) {
                        runWorkflow {
                            val response = RetrofitClient.apiService.approvePeminjaman(item.id)
                            response.data ?: error(response.message)
                        }
                    }
                }
                binding.buttonSecondary.setOnClickListener { showRejectDialog(item.id) }
            }
            "disetujui" -> {
                binding.buttonPrimary.visibility = View.VISIBLE
                binding.buttonPrimary.setText(R.string.handover_action)
                binding.buttonPrimary.setOnClickListener {
                    DialogHelper.confirm(
                        this,
                        getString(R.string.handover_action),
                        getString(R.string.handover_confirmation),
                        getString(R.string.handover_action)
                    ) {
                        runWorkflow {
                            val response = RetrofitClient.apiService.handoverPeminjaman(item.id)
                            response.data ?: error(response.message)
                        }
                    }
                }
            }
        }
    }

    private fun showRejectDialog(id: Long) {
        val dialogBinding = DialogRejectBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reject_rental)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.reject) { _, _ ->
                runWorkflow {
                    val response = RetrofitClient.apiService.rejectPeminjaman(
                        id,
                        PenolakanPeminjamanRequest(
                            dialogBinding.inputReason.text?.toString()?.trim()?.ifBlank { null }
                        )
                    )
                    response.data ?: error(response.message)
                }
            }
            .show()
    }

    private fun runWorkflow(action: suspend () -> PeminjamanDetailResponse) {
        binding.buttonPrimary.isEnabled = false
        binding.buttonSecondary.isEnabled = false
        lifecycleScope.launch {
            try {
                showContent(action())
                setResult(RESULT_OK)
                Snackbar.make(binding.root, R.string.data_updated, Snackbar.LENGTH_SHORT).show()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
                loadDetail()
            }
        }
    }

    private fun showLoading() {
        binding.progress.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun createIntent(context: Context, id: Long): Intent {
            return Intent(context, PeminjamanDetailActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

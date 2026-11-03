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
import com.contoh.rentalhp.data.model.PengembalianDetailResponse
import com.contoh.rentalhp.databinding.ActivityPengembalianDetailBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.DialogHelper
import com.contoh.rentalhp.ui.petugas.VerifikasiPengembalianActivity
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.StatusUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PengembalianDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityPengembalianDetailBinding
    private val itemAdapter = ReturnDetailItemAdapter()
    private var returnId = 0L
    private val verificationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) setResult(RESULT_OK)
        loadDetail()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengembalianDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        returnId = intent.getLongExtra(EXTRA_ID, 0L)
        if (returnId < 1) {
            finish()
            return
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerItems.apply {
            layoutManager = LinearLayoutManager(this@PengembalianDetailActivity)
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
                val response = RetrofitClient.apiService.getPengembalianDetail(returnId)
                showContent(response.data ?: error(response.message))
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun showContent(item: PengembalianDetailResponse) {
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
        binding.textId.text = getString(R.string.return_number, item.id)
        binding.textRental.text = getString(R.string.rental_number, item.idPeminjaman)
        binding.textBorrower.text = getString(R.string.borrower_value, item.namaPeminjam)
        binding.textDate.text = getString(R.string.return_date_value, FormatUtil.formatDate(item.tanggalKembali))
        StatusUtil.applyReturnTiming(binding.textDelay, item.keterlambatanPengembalian)
        binding.textFine.text = getString(R.string.fine_value, FormatUtil.formatRupiah(item.denda))
        binding.textNote.text = getString(R.string.note_value, item.catatan ?: getString(R.string.not_available))
        itemAdapter.submitList(item.items)
        binding.buttonPrimary.visibility = View.GONE
        binding.buttonSecondary.visibility = View.GONE
        val role = SessionManager.getInstance(this).role
        if (role == SessionManager.ROLE_PETUGAS || role == SessionManager.ROLE_ADMIN) {
            binding.buttonPrimary.visibility = View.VISIBLE
            binding.buttonPrimary.setText(R.string.verify_return)
            binding.buttonPrimary.setOnClickListener {
                verificationLauncher.launch(VerifikasiPengembalianActivity.createIntent(this, item.id))
            }
        }
        if (role == SessionManager.ROLE_ADMIN) {
            binding.buttonSecondary.visibility = View.VISIBLE
            binding.buttonSecondary.setText(R.string.delete)
            binding.buttonSecondary.setOnClickListener {
                DialogHelper.confirm(
                    this,
                    getString(R.string.delete_return),
                    getString(R.string.delete_return_confirmation),
                    getString(R.string.delete)
                ) { deleteReturn(item.id) }
            }
        }
    }

    private fun deleteReturn(id: Long) {
        binding.buttonPrimary.isEnabled = false
        binding.buttonSecondary.isEnabled = false
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePengembalian(id)
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

    private fun showLoading() {
        binding.progress.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    companion object {
        const val EXTRA_ID = "extra_id"

        fun createIntent(context: Context, id: Long): Intent {
            return Intent(context, PengembalianDetailActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

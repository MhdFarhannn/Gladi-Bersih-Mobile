package com.contoh.rentalhp.ui.peminjam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.databinding.ActivityAlatDetailBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.PhoneImageLoader
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.StatusUtil
import kotlinx.coroutines.launch

class AlatDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityAlatDetailBinding
    private var alatId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alatId = intent.getLongExtra(EXTRA_ID, 0L)
        if (alatId < 1) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonRetry.setOnClickListener { loadDetail() }
        loadDetail()
    }

    private fun loadDetail() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAlatDetail(alatId)
                showContent(response.data ?: error(response.message))
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun showContent(item: AlatResponse) {
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
        binding.textName.text = item.nama
        binding.textCategory.text = item.kategori
        binding.textPrice.text = getString(R.string.price_per_day, FormatUtil.formatRupiah(item.hargaSewa))
        binding.textStock.text = resources.getQuantityString(
            R.plurals.available_units,
            item.stokTersedia,
            item.stokTersedia
        )
        binding.textDescription.text = item.deskripsi?.takeIf { it.isNotBlank() }
            ?: getString(R.string.no_description)
        binding.imagePhone.contentDescription = getString(R.string.phone_image_named, item.nama)
        StatusUtil.apply(binding.textStatus, if (item.stokTersedia > 0) "tersedia" else "tidak_tersedia")
        PhoneImageLoader.load(binding.imagePhone, item.imagePath, item.nama)
        binding.buttonRent.isEnabled = item.stokTersedia > 0
        binding.buttonRent.text = if (item.stokTersedia > 0) {
            getString(R.string.rent_phone)
        } else {
            getString(R.string.stock_unavailable)
        }
        binding.buttonRent.setOnClickListener {
            startActivity(PeminjamanFormActivity.createIntent(this, item.id))
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
            return Intent(context, AlatDetailActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

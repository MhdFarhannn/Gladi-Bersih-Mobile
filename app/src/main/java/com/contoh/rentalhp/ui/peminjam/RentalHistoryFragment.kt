package com.contoh.rentalhp.ui.peminjam

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PeminjamanResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.util.FormatUtil

class RentalHistoryFragment : BasePagedListFragment<PeminjamanResponse>() {
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_rental_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_rental_borrower_message)

    override suspend fun requestPage(
        page: Int,
        pageSize: Int,
        search: String?
    ): PagedData<PeminjamanResponse> {
        val response = RetrofitClient.apiService.getPeminjaman(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: PeminjamanResponse): ListRow<PeminjamanResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.rental_list_title, item.id),
            subtitle = getString(
                com.contoh.rentalhp.R.string.date_range_value,
                FormatUtil.formatDate(item.tanggalPeminjaman),
                FormatUtil.formatDate(item.tanggalRencanaKembali)
            ),
            meta = getString(com.contoh.rentalhp.R.string.total_short, FormatUtil.formatRupiah(item.totalBiaya)),
            status = item.status,
            primaryAction = if (item.status == "dipinjam") getString(com.contoh.rentalhp.R.string.return_action) else null
        )
    }

    override fun onItemClick(item: PeminjamanResponse) {
        detailLauncher.launch(PeminjamanDetailActivity.createIntent(requireContext(), item.id))
    }

    override fun onPrimaryAction(item: PeminjamanResponse) {
        detailLauncher.launch(PengembalianFormActivity.createIntent(requireContext(), item.id))
    }
}

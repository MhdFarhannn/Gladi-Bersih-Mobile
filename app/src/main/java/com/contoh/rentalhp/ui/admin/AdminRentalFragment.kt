package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PeminjamanResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.peminjam.PeminjamanDetailActivity
import com.contoh.rentalhp.util.FormatUtil

class AdminRentalFragment : BasePagedListFragment<PeminjamanResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_borrower)
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_rental_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_rental_admin_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<PeminjamanResponse> {
        val response = RetrofitClient.apiService.getPeminjaman(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: PeminjamanResponse): ListRow<PeminjamanResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.namaPeminjam),
            subtitle = getString(
                com.contoh.rentalhp.R.string.date_range_value,
                FormatUtil.formatDate(item.tanggalPeminjaman),
                FormatUtil.formatDate(item.tanggalRencanaKembali)
            ),
            meta = FormatUtil.formatRupiah(item.totalBiaya),
            status = item.status
        )
    }

    override fun onItemClick(item: PeminjamanResponse) {
        detailLauncher.launch(PeminjamanDetailActivity.createIntent(requireContext(), item.id))
    }
}

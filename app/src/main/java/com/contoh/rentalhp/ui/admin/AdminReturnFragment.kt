package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PengembalianResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.peminjam.PengembalianDetailActivity
import com.contoh.rentalhp.util.FormatUtil

class AdminReturnFragment : BasePagedListFragment<PengembalianResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_borrower)
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_return_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_return_admin_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<PengembalianResponse> {
        val response = RetrofitClient.apiService.getPengembalian(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: PengembalianResponse): ListRow<PengembalianResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.namaPeminjam),
            subtitle = FormatUtil.formatDate(item.tanggalKembali),
            meta = getString(
                com.contoh.rentalhp.R.string.return_delay_fine_value,
                resources.getQuantityString(
                    com.contoh.rentalhp.R.plurals.late_days,
                    item.keterlambatanPengembalian,
                    item.keterlambatanPengembalian
                ),
                FormatUtil.formatRupiah(item.denda)
            ),
            status = item.statusPeminjaman
        )
    }

    override fun onItemClick(item: PengembalianResponse) {
        detailLauncher.launch(PengembalianDetailActivity.createIntent(requireContext(), item.id))
    }
}

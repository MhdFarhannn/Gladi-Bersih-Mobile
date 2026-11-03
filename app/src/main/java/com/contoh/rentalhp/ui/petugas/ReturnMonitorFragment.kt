package com.contoh.rentalhp.ui.petugas

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PengembalianResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.peminjam.PengembalianDetailActivity
import com.contoh.rentalhp.util.FormatUtil

class ReturnMonitorFragment : BasePagedListFragment<PengembalianResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_borrower)
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_return_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_return_staff_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<PengembalianResponse> {
        val response = RetrofitClient.apiService.getPengembalian(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: PengembalianResponse): ListRow<PengembalianResponse> {
        val delay = if (item.keterlambatanPengembalian > 0) {
            resources.getQuantityString(
                com.contoh.rentalhp.R.plurals.late_days,
                item.keterlambatanPengembalian,
                item.keterlambatanPengembalian
            )
        } else {
            getString(com.contoh.rentalhp.R.string.on_time)
        }
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.namaPeminjam),
            subtitle = getString(com.contoh.rentalhp.R.string.date_and_text, FormatUtil.formatDate(item.tanggalKembali), delay),
            meta = getString(com.contoh.rentalhp.R.string.fine_short, FormatUtil.formatRupiah(item.denda)),
            status = item.statusPeminjaman,
            primaryAction = getString(com.contoh.rentalhp.R.string.verify_short_action)
        )
    }

    override fun onItemClick(item: PengembalianResponse) = openDetail(item)
    override fun onPrimaryAction(item: PengembalianResponse) = openDetail(item)

    private fun openDetail(item: PengembalianResponse) {
        detailLauncher.launch(PengembalianDetailActivity.createIntent(requireContext(), item.id))
    }
}

package com.contoh.rentalhp.ui.petugas

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.MainActivity
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PeminjamanResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.peminjam.PeminjamanDetailActivity
import com.contoh.rentalhp.util.FormatUtil

class RequestQueueFragment : BasePagedListFragment<PeminjamanResponse>() {
    override val welcomeMessage: String
        get() = getString(R.string.staff_welcome)
    override val shortcutText: String
        get() = getString(R.string.handover_shortcut)
    override val searchHint: String
        get() = getString(R.string.search_borrower)
    override fun summaryText(count: Long): String = resources.getQuantityString(
        R.plurals.queue_count, count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), count
    )
    override fun onShortcutClick() {
        (activity as? MainActivity)?.openDestination(R.id.menu_handover)
    }
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_queue_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_queue_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<PeminjamanResponse> {
        val response = RetrofitClient.apiService.getPeminjaman(page, pageSize, search, "diajukan")
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
            status = item.status,
            primaryAction = getString(com.contoh.rentalhp.R.string.review_action)
        )
    }

    override fun onItemClick(item: PeminjamanResponse) = openDetail(item)
    override fun onPrimaryAction(item: PeminjamanResponse) = openDetail(item)

    private fun openDetail(item: PeminjamanResponse) {
        detailLauncher.launch(PeminjamanDetailActivity.createIntent(requireContext(), item.id))
    }
}

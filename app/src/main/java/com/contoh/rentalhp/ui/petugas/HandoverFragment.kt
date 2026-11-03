package com.contoh.rentalhp.ui.petugas

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.PeminjamanResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.peminjam.PeminjamanDetailActivity
import com.contoh.rentalhp.util.FormatUtil

class HandoverFragment : BasePagedListFragment<PeminjamanResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_borrower)
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refreshList(showLoading = false) }

    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_handover_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_handover_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<PeminjamanResponse> {
        val response = RetrofitClient.apiService.getPeminjaman(page, pageSize, search, "disetujui")
        return response.data ?: error(response.message)
    }

    override fun createRow(item: PeminjamanResponse): ListRow<PeminjamanResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.namaPeminjam),
            subtitle = FormatUtil.formatDate(item.tanggalPeminjaman),
            meta = getString(com.contoh.rentalhp.R.string.officer_value, item.namaPetugas ?: getString(com.contoh.rentalhp.R.string.not_available)),
            status = item.status,
            primaryAction = getString(com.contoh.rentalhp.R.string.handover_short_action)
        )
    }

    override fun onItemClick(item: PeminjamanResponse) {
        detailLauncher.launch(PeminjamanDetailActivity.createIntent(requireContext(), item.id))
    }

    override fun onPrimaryAction(item: PeminjamanResponse) {
        confirmMutation(
            getString(com.contoh.rentalhp.R.string.handover_action),
            getString(com.contoh.rentalhp.R.string.handover_named_confirmation, item.namaPeminjam),
            getString(com.contoh.rentalhp.R.string.handover_short_action)
        ) {
            RetrofitClient.apiService.handoverPeminjaman(item.id).message
        }
    }
}

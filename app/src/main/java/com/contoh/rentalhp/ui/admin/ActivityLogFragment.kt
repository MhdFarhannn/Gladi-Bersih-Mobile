package com.contoh.rentalhp.ui.admin

import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.LogAktivitasResponse
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.util.FormatUtil

class ActivityLogFragment : BasePagedListFragment<LogAktivitasResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_activity)
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_log_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_log_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<LogAktivitasResponse> {
        val response = RetrofitClient.apiService.getLogAktivitas(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: LogAktivitasResponse): ListRow<LogAktivitasResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = item.namaUser ?: getString(com.contoh.rentalhp.R.string.system_user),
            subtitle = item.aktivitas,
            meta = item.username?.let {
                getString(
                    com.contoh.rentalhp.R.string.username_date_value,
                    it,
                    FormatUtil.formatDateTime(item.waktu)
                )
            } ?: FormatUtil.formatDateTime(item.waktu),
            clickable = false
        )
    }
}

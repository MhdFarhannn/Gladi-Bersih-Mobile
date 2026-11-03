package com.contoh.rentalhp.ui.peminjam

import android.content.Intent
import com.contoh.rentalhp.MainActivity
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.util.FormatUtil

class CatalogFragment : BasePagedListFragment<AlatResponse>() {
    override val usePhoneGrid = true
    override val welcomeMessage: String
        get() = getString(R.string.catalog_welcome)
    override val shortcutText: String
        get() = getString(R.string.rental_history_shortcut)
    override val searchHint: String
        get() = getString(R.string.search_phone)
    override fun summaryText(count: Long): String = resources.getQuantityString(
        R.plurals.catalog_count, count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), count
    )
    override fun onShortcutClick() {
        (activity as? MainActivity)?.openDestination(R.id.menu_rental_history)
    }
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_catalog_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_catalog_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<AlatResponse> {
        val response = RetrofitClient.apiService.getAlat(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: AlatResponse): ListRow<AlatResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = item.nama,
            subtitle = item.kategori,
            meta = getString(R.string.price_per_day, FormatUtil.formatRupiah(item.hargaSewa)),
            supportingText = resources.getQuantityString(R.plurals.available_units, item.stokTersedia, item.stokTersedia),
            status = if (item.stokTersedia > 0) "tersedia" else "tidak_tersedia",
            imageUrl = item.imagePath,
            showImage = true
        )
    }

    override fun onItemClick(item: AlatResponse) {
        startActivity(AlatDetailActivity.createIntent(requireContext(), item.id))
    }
}

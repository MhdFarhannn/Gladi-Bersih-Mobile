package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.KategoriResponse
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow

class CategoryManagementFragment : BasePagedListFragment<KategoriResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_categories)
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == android.app.Activity.RESULT_OK) refreshList(showLoading = false) }

    override val showAddButton = true
    override val addButtonText: String
        get() = getString(com.contoh.rentalhp.R.string.add_category)
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_category_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_category_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<KategoriResponse> {
        val response = RetrofitClient.apiService.getKategori(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: KategoriResponse): ListRow<KategoriResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = item.nama,
            subtitle = getString(com.contoh.rentalhp.R.string.category_id_value, item.id),
            primaryAction = getString(com.contoh.rentalhp.R.string.edit),
            secondaryAction = getString(com.contoh.rentalhp.R.string.delete)
        )
    }

    override fun onAddClick() {
        formLauncher.launch(CategoryFormActivity.createIntent(requireContext()))
    }

    override fun onItemClick(item: KategoriResponse) = edit(item)
    override fun onPrimaryAction(item: KategoriResponse) = edit(item)

    override fun onSecondaryAction(item: KategoriResponse) {
        confirmMutation(
            getString(com.contoh.rentalhp.R.string.delete_category),
            getString(com.contoh.rentalhp.R.string.delete_category_confirmation, item.nama),
            getString(com.contoh.rentalhp.R.string.delete)
        ) {
            RetrofitClient.apiService.deleteKategori(item.id).message
        }
    }

    private fun edit(item: KategoriResponse) {
        formLauncher.launch(CategoryFormActivity.createIntent(requireContext(), item.id))
    }
}

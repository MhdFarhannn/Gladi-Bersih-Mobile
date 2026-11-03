package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.util.FormatUtil

class AlatManagementFragment : BasePagedListFragment<AlatResponse>() {
    override val usePhoneGrid = true
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_phone)
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == android.app.Activity.RESULT_OK) refreshList(showLoading = false) }

    override val showAddButton = true
    override val addButtonText: String
        get() = getString(com.contoh.rentalhp.R.string.add_phone_model)
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_model_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_model_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<AlatResponse> {
        val response = RetrofitClient.apiService.getAlat(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: AlatResponse): ListRow<AlatResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.nama),
            subtitle = item.kategori,
            meta = getString(com.contoh.rentalhp.R.string.price_per_day, FormatUtil.formatRupiah(item.hargaSewa)),
            supportingText = resources.getQuantityString(
                com.contoh.rentalhp.R.plurals.available_units, item.stokTersedia, item.stokTersedia
            ),
            status = if (item.stokTersedia > 0) "tersedia" else "tidak_tersedia",
            imageUrl = item.imagePath,
            showImage = true,
            primaryAction = getString(com.contoh.rentalhp.R.string.edit),
            secondaryAction = getString(com.contoh.rentalhp.R.string.delete)
        )
    }

    override fun onAddClick() {
        formLauncher.launch(AlatFormActivity.createIntent(requireContext()))
    }

    override fun onItemClick(item: AlatResponse) = edit(item)
    override fun onPrimaryAction(item: AlatResponse) = edit(item)

    override fun onSecondaryAction(item: AlatResponse) {
        confirmMutation(
            getString(com.contoh.rentalhp.R.string.delete_phone_model),
            getString(com.contoh.rentalhp.R.string.delete_phone_model_confirmation, item.nama),
            getString(com.contoh.rentalhp.R.string.delete)
        ) {
            RetrofitClient.apiService.deleteAlat(item.id).message
        }
    }

    private fun edit(item: AlatResponse) {
        formLauncher.launch(AlatFormActivity.createIntent(requireContext(), item.id))
    }
}

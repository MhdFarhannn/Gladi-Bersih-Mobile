package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.UnitAlatResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow

class UnitManagementFragment : BasePagedListFragment<UnitAlatResponse>() {
    override val searchHint: String
        get() = getString(com.contoh.rentalhp.R.string.search_units)
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == android.app.Activity.RESULT_OK) refreshList(showLoading = false) }

    override val showAddButton = true
    override val addButtonText: String
        get() = getString(com.contoh.rentalhp.R.string.add_phone_unit)
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_unit_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_unit_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<UnitAlatResponse> {
        val response = RetrofitClient.apiService.getUnitAlat(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: UnitAlatResponse): ListRow<UnitAlatResponse> {
        val editable = item.statusKetersediaan != "dipinjam"
        return ListRow(
            id = item.id,
            item = item,
            title = item.kodeInventaris,
            subtitle = getString(com.contoh.rentalhp.R.string.model_id_name_value, item.idAlat, item.alat),
            meta = getString(
                com.contoh.rentalhp.R.string.condition_value,
                com.contoh.rentalhp.util.StatusUtil.label(requireContext(), item.statusBarang)
            ),
            status = item.statusKetersediaan,
            primaryAction = if (editable) getString(com.contoh.rentalhp.R.string.edit) else null,
            secondaryAction = if (editable) getString(com.contoh.rentalhp.R.string.delete) else null,
            clickable = editable
        )
    }

    override fun onAddClick() {
        formLauncher.launch(UnitFormActivity.createIntent(requireContext()))
    }

    override fun onItemClick(item: UnitAlatResponse) = edit(item)
    override fun onPrimaryAction(item: UnitAlatResponse) = edit(item)

    override fun onSecondaryAction(item: UnitAlatResponse) {
        confirmMutation(
            getString(com.contoh.rentalhp.R.string.delete_unit),
            getString(com.contoh.rentalhp.R.string.delete_unit_confirmation, item.kodeInventaris),
            getString(com.contoh.rentalhp.R.string.delete)
        ) {
            RetrofitClient.apiService.deleteUnitAlat(item.id).message
        }
    }

    private fun edit(item: UnitAlatResponse) {
        formLauncher.launch(UnitFormActivity.createIntent(requireContext(), item.id))
    }
}

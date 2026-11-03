package com.contoh.rentalhp.ui.admin

import androidx.activity.result.contract.ActivityResultContracts
import com.contoh.rentalhp.MainActivity
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.data.model.UserResponse
import com.contoh.rentalhp.ui.common.BasePagedListFragment
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.util.FormatUtil

class UserManagementFragment : BasePagedListFragment<UserResponse>() {
    override val welcomeMessage: String
        get() = getString(R.string.admin_welcome)
    override val shortcutText: String
        get() = getString(R.string.manage_models_shortcut)
    override val searchHint: String
        get() = getString(R.string.search_users)
    override fun summaryText(count: Long): String = resources.getQuantityString(
        R.plurals.users_count, count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), count
    )
    override fun onShortcutClick() {
        (activity as? MainActivity)?.openDestination(R.id.menu_phones)
    }
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == android.app.Activity.RESULT_OK) refreshList(showLoading = false) }

    override val showAddButton = true
    override val addButtonText: String
        get() = getString(com.contoh.rentalhp.R.string.add_user)
    override val emptyTitle: String
        get() = getString(com.contoh.rentalhp.R.string.empty_user_title)
    override val emptyMessage: String
        get() = getString(com.contoh.rentalhp.R.string.empty_user_message)

    override suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<UserResponse> {
        val response = RetrofitClient.apiService.getUsers(page, pageSize, search)
        return response.data ?: error(response.message)
    }

    override fun createRow(item: UserResponse): ListRow<UserResponse> {
        return ListRow(
            id = item.id,
            item = item,
            title = getString(com.contoh.rentalhp.R.string.id_name_value, item.id, item.nama),
            subtitle = getString(com.contoh.rentalhp.R.string.username_phone_value, item.username, item.noTelepon),
            meta = getString(com.contoh.rentalhp.R.string.created_at_value, FormatUtil.formatDateTime(item.createdAt)),
            status = item.role,
            primaryAction = getString(com.contoh.rentalhp.R.string.edit),
            secondaryAction = getString(com.contoh.rentalhp.R.string.delete)
        )
    }

    override fun onAddClick() {
        formLauncher.launch(UserFormActivity.createIntent(requireContext()))
    }

    override fun onItemClick(item: UserResponse) = edit(item)
    override fun onPrimaryAction(item: UserResponse) = edit(item)

    override fun onSecondaryAction(item: UserResponse) {
        confirmMutation(
            title = getString(com.contoh.rentalhp.R.string.delete_user),
            message = getString(com.contoh.rentalhp.R.string.delete_user_confirmation, item.nama),
            positiveText = getString(com.contoh.rentalhp.R.string.delete)
        ) {
            RetrofitClient.apiService.deleteUser(item.id).message
        }
    }

    private fun edit(item: UserResponse) {
        formLauncher.launch(UserFormActivity.createIntent(requireContext(), item.id))
    }
}

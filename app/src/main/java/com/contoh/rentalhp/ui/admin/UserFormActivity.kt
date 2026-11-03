package com.contoh.rentalhp.ui.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.UserCreateRequest
import com.contoh.rentalhp.data.model.UserResponse
import com.contoh.rentalhp.data.model.UserUpdateRequest
import com.contoh.rentalhp.databinding.ActivityUserFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.PhoneValidationUtil
import com.contoh.rentalhp.util.StatusUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class UserFormActivity : BaseActivity() {
    private lateinit var binding: ActivityUserFormBinding
    private var userId = 0L
    private var selectedRole = SessionManager.ROLE_PETUGAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getLongExtra(EXTRA_ID, 0L)
        binding.layoutPassword.hint = getString(if (userId > 0) R.string.password_optional_edit else R.string.password)
        binding.toolbar.title = if (userId > 0) getString(R.string.edit_user) else getString(R.string.add_user)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupRoleInput()
        binding.buttonRetry.setOnClickListener { loadUser() }
        binding.buttonSave.setOnClickListener { save() }
        if (userId > 0) loadUser() else showContent()
    }

    private fun setupRoleInput() {
        val roles = if (userId > 0) {
            listOf(SessionManager.ROLE_ADMIN, SessionManager.ROLE_PETUGAS, SessionManager.ROLE_PEMINJAM)
        } else {
            listOf(SessionManager.ROLE_ADMIN, SessionManager.ROLE_PETUGAS)
        }
        val labels = roles.map { StatusUtil.label(this, it) }
        binding.inputRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, labels))
        binding.inputRole.setText(StatusUtil.label(this, selectedRole), false)
        binding.inputRole.setOnItemClickListener { parent, _, position, _ ->
            val selectedLabel = parent.getItemAtPosition(position).toString()
            val selectedIndex = labels.indexOf(selectedLabel)
            if (selectedIndex >= 0) selectedRole = roles[selectedIndex]
        }
    }

    private fun loadUser() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUser(userId)
                bindUser(response.data ?: error(response.message))
                showContent()
            } catch (error: Throwable) {
                showError(ApiErrorHelper.getMessage(error))
            }
        }
    }

    private fun bindUser(user: UserResponse) {
        binding.inputUsername.setText(user.username)
        binding.inputName.setText(user.nama)
        binding.inputPhone.setText(user.noTelepon)
        binding.inputAddress.setText(user.alamat.orEmpty())
        selectedRole = user.role
        binding.inputRole.setText(StatusUtil.label(this, user.role), false)
    }

    private fun save() {
        val username = binding.inputUsername.text?.toString()?.trim().orEmpty()
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val phone = binding.inputPhone.text?.toString()?.trim().orEmpty()
        val address = binding.inputAddress.text?.toString()?.trim()?.ifBlank { null }
        val password = binding.inputPassword.text?.toString().orEmpty()
        var valid = validateRequired(binding.layoutUsername, username)
        valid = validateRequired(binding.layoutName, name) && valid
        binding.layoutPhone.error = when {
            phone.isBlank() -> getString(R.string.required_field)
            !PhoneValidationUtil.isValid(phone) -> getString(R.string.invalid_phone)
            else -> null
        }
        valid = binding.layoutPhone.error == null && valid
        binding.layoutPassword.error = when {
            userId == 0L && password.isBlank() -> getString(R.string.required_field)
            password.isNotBlank() && password.length < 8 -> getString(R.string.password_minimum)
            else -> null
        }
        valid = binding.layoutPassword.error == null && valid
        if (!valid) return

        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = if (userId > 0) {
                    RetrofitClient.apiService.updateUser(
                        userId,
                        UserUpdateRequest(username, name, password.ifBlank { null }, phone, address, selectedRole)
                    )
                } else {
                    RetrofitClient.apiService.createUser(
                        UserCreateRequest(username, name, password, phone, address, selectedRole)
                    )
                }
                if (response.data == null) error(response.message)
                setResult(RESULT_OK)
                finish()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                if (!isFinishing) setSubmitting(false)
            }
        }
    }

    private fun validateRequired(layout: TextInputLayout, value: String): Boolean {
        layout.error = if (value.isBlank()) getString(R.string.required_field) else null
        return layout.error == null
    }

    private fun showLoading() {
        binding.progress.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.content.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    private fun setSubmitting(submitting: Boolean) {
        binding.buttonSave.isEnabled = !submitting
        binding.progressSave.visibility = if (submitting) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun createIntent(context: Context, id: Long = 0L): Intent {
            return Intent(context, UserFormActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }
}

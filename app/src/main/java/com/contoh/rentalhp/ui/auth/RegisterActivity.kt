package com.contoh.rentalhp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.RegisterRequest
import com.contoh.rentalhp.databinding.ActivityRegisterBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.util.PhoneValidationUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonRegister.setOnClickListener { register() }
    }

    private fun register() {
        val username = binding.inputUsername.text?.toString()?.trim().orEmpty()
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val phone = binding.inputPhone.text?.toString()?.trim().orEmpty()
        val address = binding.inputAddress.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val confirmation = binding.inputPasswordConfirmation.text?.toString().orEmpty()

        var valid = true
        valid = validateRequired(binding.layoutUsername, username) && valid
        valid = validateRequired(binding.layoutName, name) && valid
        binding.layoutPhone.error = when {
            phone.isBlank() -> getString(R.string.required_field)
            !PhoneValidationUtil.isValid(phone) -> getString(R.string.invalid_phone)
            else -> null
        }
        valid = binding.layoutPhone.error == null && valid
        binding.layoutPassword.error = when {
            password.isBlank() -> getString(R.string.required_field)
            password.length < 8 -> getString(R.string.password_minimum)
            else -> null
        }
        valid = binding.layoutPassword.error == null && valid
        binding.layoutPasswordConfirmation.error = when {
            confirmation.isBlank() -> getString(R.string.required_field)
            confirmation != password -> getString(R.string.password_not_match)
            else -> null
        }
        valid = binding.layoutPasswordConfirmation.error == null && valid
        if (!valid) return

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        username = username,
                        nama = name,
                        password = password,
                        noTelepon = phone,
                        alamat = address.ifBlank { null }
                    )
                )
                if (response.data == null) error(response.message)
                setResult(
                    RESULT_OK,
                    Intent().putExtra(EXTRA_USERNAME, username)
                )
                finish()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                if (!isFinishing) setLoading(false)
            }
        }
    }

    private fun validateRequired(layout: com.google.android.material.textfield.TextInputLayout, value: String): Boolean {
        layout.error = if (value.isBlank()) getString(R.string.required_field) else null
        return layout.error == null
    }

    private fun setLoading(loading: Boolean) {
        binding.buttonRegister.isEnabled = !loading
        binding.progressRegister.visibility = if (loading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }
}

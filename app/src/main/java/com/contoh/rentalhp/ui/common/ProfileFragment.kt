package com.contoh.rentalhp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.contoh.rentalhp.MainActivity
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.databinding.FragmentProfileBinding
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.StatusUtil
import com.contoh.rentalhp.util.ThemePreference
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val session = SessionManager.getInstance(requireContext())
        val user = session.currentUser
        binding?.apply {
            textName.text = user?.nama.orEmpty()
            textInitial.text = user?.nama?.trim()?.take(1)?.uppercase().orEmpty()
            textUsername.text = getString(R.string.username_value, user?.username.orEmpty())
            textRole.text = getString(
                R.string.role_value,
                user?.role?.let { StatusUtil.label(requireContext(), it) }.orEmpty()
            )
            textPhone.text = getString(R.string.phone_value, user?.noTelepon.orEmpty())
            textAddress.text = getString(R.string.address_value, user?.alamat ?: getString(R.string.not_available))
            buttonLogout.setOnClickListener { logout(session) }
            val themeLabels = resources.getStringArray(R.array.theme_labels)
            val themeModes = listOf(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES
            )
            inputTheme.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, themeLabels))
            inputTheme.setText(themeLabels[themeModes.indexOf(ThemePreference.currentMode(requireContext())).coerceAtLeast(0)], false)
            inputTheme.setOnItemClickListener { _, _, position, _ ->
                ThemePreference.setMode(requireContext(), themeModes[position])
            }
        }
    }

    private fun logout(session: SessionManager) {
        val currentBinding = binding ?: return
        currentBinding.buttonLogout.isEnabled = false
        currentBinding.progressLogout.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            var errorMessage: String? = null
            try {
                RetrofitClient.apiService.logout()
            } catch (error: Throwable) {
                errorMessage = ApiErrorHelper.getMessage(error)
            } finally {
                session.clearSession()
                errorMessage?.let {
                    Snackbar.make(currentBinding.root, it, Snackbar.LENGTH_SHORT).show()
                }
                (activity as? MainActivity)?.openLogin()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

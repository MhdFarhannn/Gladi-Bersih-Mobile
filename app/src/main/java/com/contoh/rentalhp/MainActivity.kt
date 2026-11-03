package com.contoh.rentalhp

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.contoh.rentalhp.databinding.ActivityMainBinding
import com.contoh.rentalhp.databinding.NavHeaderBinding
import com.contoh.rentalhp.ui.auth.LoginActivity
import com.contoh.rentalhp.ui.admin.ActivityLogFragment
import com.contoh.rentalhp.ui.admin.AdminRentalFragment
import com.contoh.rentalhp.ui.admin.AdminReturnFragment
import com.contoh.rentalhp.ui.admin.AlatManagementFragment
import com.contoh.rentalhp.ui.admin.CategoryManagementFragment
import com.contoh.rentalhp.ui.admin.UnitManagementFragment
import com.contoh.rentalhp.ui.admin.UserManagementFragment
import com.contoh.rentalhp.ui.common.ProfileFragment
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.peminjam.CatalogFragment
import com.contoh.rentalhp.ui.peminjam.RentalHistoryFragment
import com.contoh.rentalhp.ui.petugas.HandoverFragment
import com.contoh.rentalhp.ui.petugas.ReportFragment
import com.contoh.rentalhp.ui.petugas.RequestQueueFragment
import com.contoh.rentalhp.ui.petugas.ReturnMonitorFragment
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.StatusUtil

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager.getInstance(this)
        if (!sessionManager.hasValidSession()) {
            openLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDrawer()

        if (savedInstanceState == null) {
            showDefaultDestination()
        } else {
            savedInstanceState.getCharSequence(STATE_TOOLBAR_TITLE)?.let { binding.toolbar.title = it }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sessionManager.isInitialized && !sessionManager.hasValidSession()) {
            openLogin(getString(R.string.session_expired))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::binding.isInitialized) outState.putCharSequence(STATE_TOOLBAR_TITLE, binding.toolbar.title)
        super.onSaveInstanceState(outState)
    }

    private fun setupDrawer() {
        val role = sessionManager.role ?: return openLogin()
        val menuRes = when (role) {
            SessionManager.ROLE_ADMIN -> R.menu.menu_admin
            SessionManager.ROLE_PETUGAS -> R.menu.menu_petugas
            SessionManager.ROLE_PEMINJAM -> R.menu.menu_peminjam
            else -> {
                sessionManager.clearSession()
                return openLogin()
            }
        }

        setSupportActionBar(binding.toolbar)
        ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_menu,
            R.string.close_menu
        ).also {
            binding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }

        binding.navigationView.menu.clear()
        binding.navigationView.inflateMenu(menuRes)
        val header = NavHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        header.textName.text = sessionManager.currentUser?.nama.orEmpty()
        StatusUtil.apply(header.textRole, role)

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalog -> showCatalog()
                R.id.menu_rental_history -> showRentalHistory()
                R.id.menu_request_queue -> showRequestQueue()
                R.id.menu_handover -> showHandover()
                R.id.menu_return_monitor -> showReturnMonitor()
                R.id.menu_reports -> showReports()
                R.id.menu_users -> showUsers()
                R.id.menu_categories -> showCategories()
                R.id.menu_phones -> showPhones()
                R.id.menu_units -> showUnits()
                R.id.menu_rental_data -> showAdminRentals()
                R.id.menu_return_data -> showAdminReturns()
                R.id.menu_activity_log -> showActivityLog()
                R.id.menu_profile -> showProfile()
                else -> false
            }.also { handled ->
                if (handled) binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun showDefaultDestination() {
        when (sessionManager.role) {
            SessionManager.ROLE_PEMINJAM -> showCatalog()
            SessionManager.ROLE_PETUGAS -> showRequestQueue()
            SessionManager.ROLE_ADMIN -> showUsers()
            else -> openLogin()
        }
    }

    private fun showProfile(): Boolean {
        binding.toolbar.title = getString(R.string.profile)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, ProfileFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_profile)
        return true
    }

    private fun showCatalog(): Boolean {
        binding.toolbar.title = getString(R.string.phone_catalog)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, CatalogFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_catalog)
        return true
    }

    private fun showRentalHistory(): Boolean {
        binding.toolbar.title = getString(R.string.rental_history)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, RentalHistoryFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_rental_history)
        return true
    }

    private fun showRequestQueue(): Boolean {
        binding.toolbar.title = getString(R.string.request_queue)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, RequestQueueFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_request_queue)
        return true
    }

    private fun showHandover(): Boolean {
        binding.toolbar.title = getString(R.string.handover)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, HandoverFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_handover)
        return true
    }

    private fun showReturnMonitor(): Boolean {
        binding.toolbar.title = getString(R.string.return_monitor)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, ReturnMonitorFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_return_monitor)
        return true
    }

    private fun showReports(): Boolean {
        binding.toolbar.title = getString(R.string.reports)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, ReportFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_reports)
        return true
    }

    private fun showUsers(): Boolean {
        binding.toolbar.title = getString(R.string.user_management)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, UserManagementFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_users)
        return true
    }

    private fun showCategories(): Boolean {
        binding.toolbar.title = getString(R.string.category_management)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, CategoryManagementFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_categories)
        return true
    }

    private fun showPhones(): Boolean {
        binding.toolbar.title = getString(R.string.phone_management)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, AlatManagementFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_phones)
        return true
    }

    private fun showUnits(): Boolean {
        binding.toolbar.title = getString(R.string.unit_management)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, UnitManagementFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_units)
        return true
    }

    private fun showAdminRentals(): Boolean {
        binding.toolbar.title = getString(R.string.rental_data)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, AdminRentalFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_rental_data)
        return true
    }

    private fun showAdminReturns(): Boolean {
        binding.toolbar.title = getString(R.string.return_data)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, AdminReturnFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_return_data)
        return true
    }

    private fun showActivityLog(): Boolean {
        binding.toolbar.title = getString(R.string.activity_log)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, ActivityLogFragment())
            .commit()
        binding.navigationView.setCheckedItem(R.id.menu_activity_log)
        return true
    }

    fun openLogin(message: String? = null) {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(LoginActivity.EXTRA_MESSAGE, message)
            }
        )
        finish()
    }

    fun openDestination(menuItemId: Int) {
        binding.navigationView.menu.performIdentifierAction(menuItemId, 0)
    }

    companion object {
        private const val STATE_TOOLBAR_TITLE = "toolbar_title"
    }
}

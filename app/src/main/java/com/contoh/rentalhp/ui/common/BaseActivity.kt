package com.contoh.rentalhp.ui.common

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.SessionEvents
import com.contoh.rentalhp.ui.auth.LoginActivity
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SessionEvents.expired.collect { openExpiredSessionLogin() }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val content = findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0) ?: return
        val targets = if (root is DrawerLayout) {
            (0 until root.childCount).map(root::getChildAt)
        } else {
            listOf(root)
        }
        val initialPadding = targets.associateWith {
            Rect(it.paddingLeft, it.paddingTop, it.paddingRight, it.paddingBottom)
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            targets.forEach { view ->
                val padding = requireNotNull(initialPadding[view])
                view.updatePadding(
                    left = padding.left + bars.left,
                    top = padding.top + bars.top,
                    right = padding.right + bars.right,
                    bottom = padding.bottom + maxOf(bars.bottom, ime.bottom)
                )
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun openExpiredSessionLogin() {
        if (this is LoginActivity) return
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(LoginActivity.EXTRA_MESSAGE, getString(R.string.session_expired))
            }
        )
        finishAffinity()
    }
}

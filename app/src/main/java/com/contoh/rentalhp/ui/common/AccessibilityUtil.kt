package com.contoh.rentalhp.ui.common

import android.view.KeyEvent
import android.view.View

object AccessibilityUtil {
    fun activateOnKeyboard(view: View) {
        view.setOnKeyListener { target, keyCode, event ->
            val activationKey = keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_SPACE
            if (activationKey && event.action == KeyEvent.ACTION_UP) {
                target.performClick()
                true
            } else {
                false
            }
        }
    }
}

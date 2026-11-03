package com.contoh.rentalhp.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemePreference {
    private const val PREFERENCES_NAME = "rental_hp_appearance"
    private const val KEY_MODE = "theme_mode"

    fun currentMode(context: Context): Int {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun apply(context: Context) {
        AppCompatDelegate.setDefaultNightMode(currentMode(context))
    }

    fun setMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit {
            putInt(KEY_MODE, mode)
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

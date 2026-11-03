package com.contoh.rentalhp

import android.app.Application
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.util.ThemePreference

class RentalHpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 33) {
            getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags("id")
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("id"))
        }
        ThemePreference.apply(this)
        ApiErrorHelper.initialize(this)
        RetrofitClient.initialize(this)
    }
}

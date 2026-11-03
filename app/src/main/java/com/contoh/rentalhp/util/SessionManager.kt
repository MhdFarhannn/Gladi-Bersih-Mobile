@file:Suppress("DEPRECATION")

package com.contoh.rentalhp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.contoh.rentalhp.data.model.AuthResponse
import com.contoh.rentalhp.data.model.UserResponse
import java.time.Instant

class SessionManager private constructor(context: Context) {
    private val preferences: SharedPreferences = createPreferences(context.applicationContext)

    val token: String?
        get() = preferences.getString(KEY_TOKEN, null)

    val role: String?
        get() = preferences.getString(KEY_ROLE, null)

    val currentUser: UserResponse?
        get() {
            val id = preferences.getLong(KEY_USER_ID, -1L)
            val username = preferences.getString(KEY_USERNAME, null)
            val nama = preferences.getString(KEY_NAME, null)
            val noTelepon = preferences.getString(KEY_PHONE, null)
            val currentRole = role
            if (id < 1 || username == null || nama == null || noTelepon == null || currentRole == null) {
                return null
            }
            return UserResponse(
                id = id,
                username = username,
                nama = nama,
                noTelepon = noTelepon,
                alamat = preferences.getString(KEY_ADDRESS, null),
                role = currentRole,
                createdAt = preferences.getString(KEY_CREATED_AT, "").orEmpty()
            )
        }

    fun saveSession(response: AuthResponse) {
        preferences.edit {
            putString(KEY_TOKEN, response.token)
            putString(KEY_EXPIRES_AT, response.expiresAt)
            putLong(KEY_USER_ID, response.user.id)
            putString(KEY_USERNAME, response.user.username)
            putString(KEY_NAME, response.user.nama)
            putString(KEY_PHONE, response.user.noTelepon)
            putString(KEY_ADDRESS, response.user.alamat)
            putString(KEY_ROLE, response.user.role)
            putString(KEY_CREATED_AT, response.user.createdAt)
        }
    }

    fun hasValidSession(): Boolean {
        val savedToken = token ?: return false
        val expiresAt = preferences.getString(KEY_EXPIRES_AT, null) ?: return false
        if (savedToken.isBlank()) return false
        return runCatching { Instant.parse(expiresAt).isAfter(Instant.now()) }.getOrDefault(false)
    }

    fun clearSession() {
        preferences.edit { clear() }
    }

    private fun createPreferences(context: Context): SharedPreferences {
        return try {
            createEncryptedPreferences(context)
        } catch (_: Exception) {
            context.deleteSharedPreferences(PREFERENCES_NAME)
            createEncryptedPreferences(context)
        }
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFERENCES_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val ROLE_ADMIN = "Admin"
        const val ROLE_PETUGAS = "Petugas"
        const val ROLE_PEMINJAM = "Peminjam"

        private const val PREFERENCES_NAME = "rental_hp_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDRESS = "address"
        private const val KEY_ROLE = "role"
        private const val KEY_CREATED_AT = "created_at"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }
    }
}

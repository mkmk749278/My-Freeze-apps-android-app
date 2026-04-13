package com.mkmk749278.myapps.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecureSettingsStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun hasPin(): Boolean = prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    suspend fun savePin(pin: String) = withContext(Dispatchers.IO) {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return@withContext false
        val hash = prefs.getString(KEY_PIN_HASH, null) ?: return@withContext false
        PinHasher.verify(pin, salt, hash)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    private companion object {
        const val FILE_NAME = "secure_settings"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}

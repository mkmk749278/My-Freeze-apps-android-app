package com.mkmk749278.myapps.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mkmk749278.myapps.model.AuthMethodState
import kotlinx.coroutines.flow.first

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

class AppPreferencesStore(private val context: Context) {
    suspend fun readAuthMethods(): AuthMethodState {
        val preferences = context.appPreferencesDataStore.data.first()
        return AuthMethodState(
            pinEnabled = preferences[PIN_ENABLED] ?: true,
            fingerprintEnabled = preferences[FINGERPRINT_ENABLED] ?: false,
            faceEnabled = preferences[FACE_ENABLED] ?: false,
        )
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        updateBoolean(PIN_ENABLED, enabled)
    }

    suspend fun setFingerprintEnabled(enabled: Boolean) {
        updateBoolean(FINGERPRINT_ENABLED, enabled)
    }

    suspend fun setFaceEnabled(enabled: Boolean) {
        updateBoolean(FACE_ENABLED, enabled)
    }

    private suspend fun updateBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private companion object {
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val FINGERPRINT_ENABLED = booleanPreferencesKey("fingerprint_enabled")
        val FACE_ENABLED = booleanPreferencesKey("face_enabled")
    }
}

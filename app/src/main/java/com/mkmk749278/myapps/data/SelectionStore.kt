package com.mkmk749278.myapps.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.selectionDataStore by preferencesDataStore(name = "app_selection")

data class SelectionState(
    val selectedPackages: Set<String>,
    val favoritePackages: Set<String>,
)

class SelectionStore(private val context: Context) {
    suspend fun readState(): SelectionState {
        val preferences = context.selectionDataStore.data.first()
        return SelectionState(
            selectedPackages = preferences[SELECTED_PACKAGES] ?: emptySet(),
            favoritePackages = preferences[FAVORITE_PACKAGES] ?: emptySet(),
        )
    }

    suspend fun setSelected(packageName: String, selected: Boolean) {
        context.selectionDataStore.edit { prefs ->
            val selectedPackages = (prefs[SELECTED_PACKAGES] ?: emptySet()).toMutableSet()
            val favoritePackages = (prefs[FAVORITE_PACKAGES] ?: emptySet()).toMutableSet()
            if (selected) {
                selectedPackages += packageName
            } else {
                selectedPackages -= packageName
                favoritePackages -= packageName
            }
            prefs[SELECTED_PACKAGES] = selectedPackages
            prefs[FAVORITE_PACKAGES] = favoritePackages
        }
    }

    suspend fun setFavorite(packageName: String, favorite: Boolean) {
        context.selectionDataStore.edit { prefs ->
            val selectedPackages = (prefs[SELECTED_PACKAGES] ?: emptySet()).toMutableSet()
            val favoritePackages = (prefs[FAVORITE_PACKAGES] ?: emptySet()).toMutableSet()
            if (favorite) {
                selectedPackages += packageName
                favoritePackages += packageName
            } else {
                favoritePackages -= packageName
            }
            prefs[SELECTED_PACKAGES] = selectedPackages
            prefs[FAVORITE_PACKAGES] = favoritePackages
        }
    }

    private companion object {
        val SELECTED_PACKAGES: Preferences.Key<Set<String>> = stringSetPreferencesKey("selected_packages")
        val FAVORITE_PACKAGES: Preferences.Key<Set<String>> = stringSetPreferencesKey("favorite_packages")
    }
}

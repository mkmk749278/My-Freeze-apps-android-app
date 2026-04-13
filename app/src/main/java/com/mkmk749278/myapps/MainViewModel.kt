package com.mkmk749278.myapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mkmk749278.myapps.data.AppPreferencesStore
import com.mkmk749278.myapps.data.ManagedAppRepository
import com.mkmk749278.myapps.data.SecureSettingsStore
import com.mkmk749278.myapps.data.SelectionStore
import com.mkmk749278.myapps.model.AppCategoryFilter
import com.mkmk749278.myapps.model.AppUiState
import com.mkmk749278.myapps.model.AuthMethodState
import com.mkmk749278.myapps.model.LockMode
import com.mkmk749278.myapps.model.ManagedApp
import com.mkmk749278.myapps.model.TabDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val selectionStore = SelectionStore(application)
    private val secureSettingsStore = SecureSettingsStore(application)
    private val appPreferencesStore = AppPreferencesStore(application)
    private val repository = ManagedAppRepository(application, selectionStore)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var sessionUnlocked = false
    private var pinResetRequested = false

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val apps = repository.loadApps()
            val selectedApps = apps.filter(ManagedApp::isSelected)
            val favoriteApps = selectedApps.filter(ManagedApp::isFavorite)
            val hasPin = secureSettingsStore.hasPin()
            val authMethods = sanitizeAuthMethods(appPreferencesStore.readAuthMethods(), hasPin)
            val accessStatus = repository.resolveBackend()
            val lockMode = when {
                pinResetRequested || !hasPin -> LockMode.Setup
                sessionUnlocked -> LockMode.Unlocked
                else -> LockMode.Locked
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                apps = apps,
                selectedApps = selectedApps,
                favoriteApps = favoriteApps,
                activeBackend = accessStatus.backend,
                rootAvailable = accessStatus.rootAvailable,
                shizukuAvailable = accessStatus.shizukuAvailable,
                shizukuPermissionGranted = accessStatus.shizukuPermissionGranted,
                lockMode = lockMode,
                authMethods = authMethods,
            )
        }
    }

    fun selectTab(tabDestination: TabDestination) {
        _uiState.value = _uiState.value.copy(selectedTab = tabDestination)
    }

    fun updateDashboardQuery(query: String) {
        _uiState.value = _uiState.value.copy(dashboardQuery = query)
    }

    fun updateDashboardFilter(filter: AppCategoryFilter) {
        _uiState.value = _uiState.value.copy(dashboardFilter = filter)
    }

    fun toggleQuickActions() {
        _uiState.value = _uiState.value.copy(quickActionsExpanded = !_uiState.value.quickActionsExpanded)
    }

    fun toggleDashboardOptions() {
        _uiState.value = _uiState.value.copy(dashboardOptionsVisible = !_uiState.value.dashboardOptionsVisible)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun createPin(pin: String, confirmation: String) {
        if (pin.length < 4 || !pin.all(Char::isDigit)) {
            _uiState.value = _uiState.value.copy(statusMessage = "Use a numeric PIN with at least 4 digits.")
            return
        }
        if (pin != confirmation) {
            _uiState.value = _uiState.value.copy(statusMessage = "PIN confirmation does not match.")
            return
        }
        viewModelScope.launch {
            secureSettingsStore.savePin(pin)
            appPreferencesStore.setPinEnabled(true)
            sessionUnlocked = true
            pinResetRequested = false
            _uiState.value = _uiState.value.copy(statusMessage = "PIN saved. App unlocked.")
            refreshAll()
        }
    }

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            val isValid = secureSettingsStore.verifyPin(pin)
            if (isValid) {
                sessionUnlocked = true
                _uiState.value = _uiState.value.copy(statusMessage = "Unlocked.")
                refreshAll()
            } else {
                _uiState.value = _uiState.value.copy(statusMessage = "Incorrect PIN.")
            }
        }
    }

    fun markUnlocked() {
        sessionUnlocked = true
        refreshAll()
    }

    fun beginPinReset() {
        sessionUnlocked = false
        pinResetRequested = true
        _uiState.value = _uiState.value.copy(lockMode = LockMode.Setup)
    }

    fun setPinEnabled(enabled: Boolean) {
        updateAuthMethods { current ->
            val updated = current.copy(pinEnabled = enabled)
            if (!updated.anyEnabled) {
                AuthUpdateResult(error = "Enable PIN or at least one biometric method.")
            } else {
                AuthUpdateResult(updatedState = updated, successMessage = if (enabled) "PIN unlock enabled." else "PIN unlock disabled.")
            }
        }
    }

    fun setFingerprintEnabled(enabled: Boolean) {
        updateAuthMethods { current ->
            val updated = current.copy(fingerprintEnabled = enabled)
            if (!updated.anyEnabled) {
                AuthUpdateResult(error = "Enable PIN or at least one biometric method.")
            } else {
                AuthUpdateResult(
                    updatedState = updated,
                    successMessage = if (enabled) "Fingerprint unlock enabled." else "Fingerprint unlock disabled.",
                )
            }
        }
    }

    fun setFaceEnabled(enabled: Boolean) {
        updateAuthMethods { current ->
            val updated = current.copy(faceEnabled = enabled)
            if (!updated.anyEnabled) {
                AuthUpdateResult(error = "Enable PIN or at least one biometric method.")
            } else {
                AuthUpdateResult(
                    updatedState = updated,
                    successMessage = if (enabled) "Face unlock enabled." else "Face unlock disabled.",
                )
            }
        }
    }

    fun onShizukuPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            statusMessage = if (granted) "Shizuku access granted." else "Shizuku access was not granted.",
        )
        refreshAll()
    }

    fun toggleAppSelection(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            selectionStore.setSelected(packageName, selected)
            refreshAll()
        }
    }

    fun freezeApp(packageName: String) {
        runMutation(action = { repository.freezeApp(packageName) }, successMessage = "App frozen.")
    }

    fun unfreezeApp(packageName: String) {
        runMutation(action = { repository.unfreezeApp(packageName) }, successMessage = "App unfrozen.")
    }

    fun freezeSelectedApps() {
        val selected = _uiState.value.selectedApps.map(ManagedApp::packageName)
        runMutation(action = { repository.freezeApps(selected) }, successMessage = "Selected apps frozen.")
    }

    fun unfreezeSelectedApps() {
        val selected = _uiState.value.selectedApps.map(ManagedApp::packageName)
        runMutation(action = { repository.unfreezeApps(selected) }, successMessage = "Selected apps unfrozen.")
    }

    fun launchSelectedApp(packageName: String) {
        viewModelScope.launch {
            val result = repository.launchApp(packageName)
            _uiState.value = _uiState.value.copy(statusMessage = result)
            refreshAll()
        }
    }

    private fun updateAuthMethods(block: (AuthMethodState) -> AuthUpdateResult) {
        viewModelScope.launch {
            if (!secureSettingsStore.hasPin()) {
                _uiState.value = _uiState.value.copy(statusMessage = "Create a PIN before changing authentication methods.")
                return@launch
            }
            val current = sanitizeAuthMethods(appPreferencesStore.readAuthMethods(), hasPin = true)
            val result = block(current)
            if (result.error != null) {
                _uiState.value = _uiState.value.copy(statusMessage = result.error)
                return@launch
            }
            val updated = result.updatedState ?: return@launch
            appPreferencesStore.setPinEnabled(updated.pinEnabled)
            appPreferencesStore.setFingerprintEnabled(updated.fingerprintEnabled)
            appPreferencesStore.setFaceEnabled(updated.faceEnabled)
            _uiState.value = _uiState.value.copy(statusMessage = result.successMessage)
            refreshAll()
        }
    }

    private fun sanitizeAuthMethods(authMethods: AuthMethodState, hasPin: Boolean): AuthMethodState {
        val normalized = authMethods.copy(pinEnabled = hasPin && authMethods.pinEnabled)
        return if (normalized.anyEnabled) normalized else normalized.copy(pinEnabled = hasPin)
    }

    private fun runMutation(action: suspend () -> Result<Unit>, successMessage: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = action()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = result.fold(onSuccess = { successMessage }, onFailure = { it.message ?: "Action failed." }),
            )
            refreshAll()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}

private data class AuthUpdateResult(
    val updatedState: AuthMethodState? = null,
    val successMessage: String? = null,
    val error: String? = null,
)

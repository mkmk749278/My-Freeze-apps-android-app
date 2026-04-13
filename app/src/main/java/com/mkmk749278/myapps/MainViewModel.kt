package com.mkmk749278.myapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mkmk749278.myapps.data.ManagedAppRepository
import com.mkmk749278.myapps.data.SecureSettingsStore
import com.mkmk749278.myapps.data.SelectionStore
import com.mkmk749278.myapps.data.RootShell
import com.mkmk749278.myapps.model.AppUiState
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
            val biometricEnabled = secureSettingsStore.isBiometricEnabled() && hasPin
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
                rootAvailable = RootShell.isRootAvailable(),
                lockMode = lockMode,
                biometricEnabled = biometricEnabled,
            )
        }
    }

    fun selectTab(tabDestination: TabDestination) {
        _uiState.value = _uiState.value.copy(selectedTab = tabDestination)
    }

    fun toggleQuickActions() {
        _uiState.value = _uiState.value.copy(
            quickActionsExpanded = !_uiState.value.quickActionsExpanded,
        )
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

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!secureSettingsStore.hasPin()) {
                _uiState.value = _uiState.value.copy(statusMessage = "Create a PIN before enabling biometrics.")
                return@launch
            }
            secureSettingsStore.setBiometricEnabled(enabled)
            _uiState.value = _uiState.value.copy(
                biometricEnabled = enabled,
                statusMessage = if (enabled) "Biometric unlock enabled." else "Biometric unlock disabled.",
            )
        }
    }

    fun toggleAppSelection(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            selectionStore.setSelected(packageName, selected)
            refreshAll()
        }
    }

    fun toggleFavorite(packageName: String, favorite: Boolean) {
        viewModelScope.launch {
            selectionStore.setFavorite(packageName, favorite)
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

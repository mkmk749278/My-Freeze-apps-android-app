package com.mkmk749278.myapps.model

data class ManagedApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val isSelected: Boolean,
    val isFavorite: Boolean,
    val isFrozen: Boolean,
    val isLaunchable: Boolean,
)

enum class TabDestination(val title: String) {
    Dashboard("Dashboard"),
    Library("App Library"),
    Settings("Settings"),
}

enum class LockMode {
    Setup,
    Locked,
    Unlocked,
}

data class AppUiState(
    val isLoading: Boolean = true,
    val apps: List<ManagedApp> = emptyList(),
    val selectedApps: List<ManagedApp> = emptyList(),
    val favoriteApps: List<ManagedApp> = emptyList(),
    val selectedTab: TabDestination = TabDestination.Dashboard,
    val rootAvailable: Boolean = false,
    val quickActionsExpanded: Boolean = false,
    val lockMode: LockMode = LockMode.Setup,
    val biometricEnabled: Boolean = false,
    val statusMessage: String? = null,
)

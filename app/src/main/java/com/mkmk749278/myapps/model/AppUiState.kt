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
    Library("Library"),
    Settings("Settings"),
}

enum class LockMode {
    Setup,
    Locked,
    Unlocked,
}

enum class AppCategoryFilter(val label: String) {
    All("All"),
    User("User"),
    System("System"),
}

enum class OperationBackend(val label: String) {
    Root("Root"),
    Shizuku("Shizuku"),
    Unavailable("Unavailable"),
}

data class AuthMethodState(
    val pinEnabled: Boolean = true,
    val fingerprintEnabled: Boolean = false,
    val faceEnabled: Boolean = false,
) {
    val hasBiometricOption: Boolean get() = fingerprintEnabled || faceEnabled
    val anyEnabled: Boolean get() = pinEnabled || hasBiometricOption
}

data class AppUiState(
    val isLoading: Boolean = true,
    val apps: List<ManagedApp> = emptyList(),
    val selectedApps: List<ManagedApp> = emptyList(),
    val favoriteApps: List<ManagedApp> = emptyList(),
    val availableTabs: List<TabDestination> = TabDestination.entries,
    val selectedTab: TabDestination = TabDestination.Dashboard,
    val activeBackend: OperationBackend = OperationBackend.Unavailable,
    val rootAvailable: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val shizukuPermissionGranted: Boolean = false,
    val quickActionsExpanded: Boolean = false,
    val dashboardOptionsVisible: Boolean = false,
    val dashboardQuery: String = "",
    val dashboardFilter: AppCategoryFilter = AppCategoryFilter.All,
    val lockMode: LockMode = LockMode.Setup,
    val authMethods: AuthMethodState = AuthMethodState(),
    val statusMessage: String? = null,
)

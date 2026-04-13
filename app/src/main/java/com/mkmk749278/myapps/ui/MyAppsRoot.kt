package com.mkmk749278.myapps.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.model.AppCategoryFilter
import com.mkmk749278.myapps.model.AppUiState
import com.mkmk749278.myapps.model.LockMode
import com.mkmk749278.myapps.model.TabDestination
import com.mkmk749278.myapps.ui.screens.DashboardScreen
import com.mkmk749278.myapps.ui.screens.LibraryScreen
import com.mkmk749278.myapps.ui.screens.LockScreen
import com.mkmk749278.myapps.ui.screens.SettingsScreen

@Composable
fun MyAppsRoot(
    uiState: AppUiState,
    snackbarHostState: SnackbarHostState,
    onSelectTab: (TabDestination) -> Unit,
    onRefresh: () -> Unit,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onToggleSelected: (String, Boolean) -> Unit,
    onToggleQuickActions: () -> Unit,
    onToggleDashboardOptions: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onUpdateDashboardQuery: (String) -> Unit,
    onUpdateDashboardFilter: (AppCategoryFilter) -> Unit,
    onCreatePin: (String, String) -> Unit,
    onUnlockWithPin: (String) -> Unit,
    onBiometricUnlock: () -> Unit,
    onPinPreferenceChanged: (Boolean) -> Unit,
    onFingerprintPreferenceChanged: (Boolean) -> Unit,
    onFacePreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onRequestShizukuPermission: suspend () -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(targetState = uiState.lockMode, label = "root-content") { lockMode ->
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                lockMode != LockMode.Unlocked -> {
                    LockScreen(
                        lockMode = lockMode,
                        authMethods = uiState.authMethods,
                        onCreatePin = onCreatePin,
                        onUnlockWithPin = onUnlockWithPin,
                        onBiometricUnlock = onBiometricUnlock,
                    )
                }

                else -> {
                    HomeScaffold(
                        uiState = uiState,
                        snackbarHostState = snackbarHostState,
                        onSelectTab = onSelectTab,
                        onRefresh = onRefresh,
                        onLaunch = onLaunch,
                        onFreeze = onFreeze,
                        onUnfreeze = onUnfreeze,
                        onToggleSelected = onToggleSelected,
                        onToggleQuickActions = onToggleQuickActions,
                        onToggleDashboardOptions = onToggleDashboardOptions,
                        onFreezeAll = onFreezeAll,
                        onUnfreezeAll = onUnfreezeAll,
                        onUpdateDashboardQuery = onUpdateDashboardQuery,
                        onUpdateDashboardFilter = onUpdateDashboardFilter,
                        onPinPreferenceChanged = onPinPreferenceChanged,
                        onFingerprintPreferenceChanged = onFingerprintPreferenceChanged,
                        onFacePreferenceChanged = onFacePreferenceChanged,
                        onBeginPinReset = onBeginPinReset,
                        onRequestShizukuPermission = onRequestShizukuPermission,
                        onShowDetails = onShowDetails,
                        onUninstall = onUninstall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    uiState: AppUiState,
    snackbarHostState: SnackbarHostState,
    onSelectTab: (TabDestination) -> Unit,
    onRefresh: () -> Unit,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onToggleSelected: (String, Boolean) -> Unit,
    onToggleQuickActions: () -> Unit,
    onToggleDashboardOptions: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onUpdateDashboardQuery: (String) -> Unit,
    onUpdateDashboardFilter: (AppCategoryFilter) -> Unit,
    onPinPreferenceChanged: (Boolean) -> Unit,
    onFingerprintPreferenceChanged: (Boolean) -> Unit,
    onFacePreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onRequestShizukuPermission: suspend () -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Apps")
                        Text(
                            text = "Backend: ${uiState.activeBackend.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                uiState.availableTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    TabDestination.Dashboard -> Icons.Rounded.Security
                                    TabDestination.Library -> Icons.Rounded.Apps
                                    TabDestination.Settings -> Icons.Rounded.Lock
                                },
                                contentDescription = tab.title,
                            )
                        },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.selectedTab == TabDestination.Dashboard,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            ) {
                QuickActionsPanel(
                    expanded = uiState.quickActionsExpanded,
                    optionsVisible = uiState.dashboardOptionsVisible,
                    onToggle = onToggleQuickActions,
                    onFreezeAll = onFreezeAll,
                    onUnfreezeAll = onUnfreezeAll,
                    onToggleDashboardOptions = onToggleDashboardOptions,
                )
            }
        },
    ) { innerPadding ->
        when (uiState.selectedTab) {
            TabDestination.Dashboard -> DashboardScreen(
                contentPadding = innerPadding,
                apps = uiState.selectedApps,
                query = uiState.dashboardQuery,
                filter = uiState.dashboardFilter,
                optionsVisible = uiState.dashboardOptionsVisible,
                onQueryChange = onUpdateDashboardQuery,
                onFilterSelected = onUpdateDashboardFilter,
                onLaunch = onLaunch,
                onFreeze = onFreeze,
                onUnfreeze = onUnfreeze,
                onShowDetails = onShowDetails,
                onUninstall = onUninstall,
            )

            TabDestination.Library -> LibraryScreen(
                contentPadding = innerPadding,
                apps = uiState.apps,
                onToggleSelected = onToggleSelected,
            )

            TabDestination.Settings -> SettingsScreen(
                contentPadding = innerPadding,
                selectedCount = uiState.selectedApps.size,
                activeBackend = uiState.activeBackend,
                rootAvailable = uiState.rootAvailable,
                shizukuAvailable = uiState.shizukuAvailable,
                shizukuPermissionGranted = uiState.shizukuPermissionGranted,
                authMethods = uiState.authMethods,
                onPinPreferenceChanged = onPinPreferenceChanged,
                onFingerprintPreferenceChanged = onFingerprintPreferenceChanged,
                onFacePreferenceChanged = onFacePreferenceChanged,
                onBeginPinReset = onBeginPinReset,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun QuickActionsPanel(
    expanded: Boolean,
    optionsVisible: Boolean,
    onToggle: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onToggleDashboardOptions: () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(visible = expanded, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                ExtendedFloatingActionButton(
                    text = { Text("Freeze All") },
                    icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                    onClick = onFreezeAll,
                )
                ExtendedFloatingActionButton(
                    text = { Text("Unfreeze All") },
                    icon = { Icon(Icons.Rounded.Security, contentDescription = null) },
                    onClick = onUnfreezeAll,
                )
                ExtendedFloatingActionButton(
                    text = { Text(if (optionsVisible) "Hide Options" else "Show Options") },
                    icon = { Icon(Icons.Rounded.Apps, contentDescription = null) },
                    onClick = onToggleDashboardOptions,
                )
            }
        }
        ExtendedFloatingActionButton(
            text = { Text(if (expanded) "Close" else "Quick Actions") },
            icon = { Icon(Icons.Rounded.Security, contentDescription = null) },
            onClick = onToggle,
        )
    }
}

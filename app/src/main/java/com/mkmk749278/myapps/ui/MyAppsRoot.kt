package com.mkmk749278.myapps.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
    onToggleFavorite: (String, Boolean) -> Unit,
    onToggleQuickActions: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onCreatePin: (String, String) -> Unit,
    onUnlockWithPin: (String) -> Unit,
    onBiometricUnlock: () -> Unit,
    onBiometricPreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.lockMode != LockMode.Unlocked -> {
                LockScreen(
                    lockMode = uiState.lockMode,
                    biometricEnabled = uiState.biometricEnabled,
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
                    onToggleFavorite = onToggleFavorite,
                    onToggleQuickActions = onToggleQuickActions,
                    onFreezeAll = onFreezeAll,
                    onUnfreezeAll = onUnfreezeAll,
                    onBiometricPreferenceChanged = onBiometricPreferenceChanged,
                    onBeginPinReset = onBeginPinReset,
                    onShowDetails = onShowDetails,
                    onUninstall = onUninstall,
                )
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
    onToggleFavorite: (String, Boolean) -> Unit,
    onToggleQuickActions: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onBiometricPreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "My Apps", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (uiState.rootAvailable) "Root access ready" else "Root access unavailable",
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
                TabDestination.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    TabDestination.Dashboard -> Icons.Rounded.ChevronRight
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
            QuickActionsPanel(
                expanded = uiState.quickActionsExpanded,
                onToggle = onToggleQuickActions,
                onFreezeAll = onFreezeAll,
                onUnfreezeAll = onUnfreezeAll,
                onRefresh = onRefresh,
            )
        },
    ) { innerPadding ->
        when (uiState.selectedTab) {
            TabDestination.Dashboard -> DashboardScreen(
                contentPadding = innerPadding,
                selectedApps = uiState.selectedApps,
                favoriteApps = uiState.favoriteApps,
                onLaunch = onLaunch,
                onFreeze = onFreeze,
                onUnfreeze = onUnfreeze,
                onToggleFavorite = onToggleFavorite,
                onShowDetails = onShowDetails,
                onUninstall = onUninstall,
            )
            TabDestination.Library -> LibraryScreen(
                contentPadding = innerPadding,
                apps = uiState.apps,
                onLaunch = onLaunch,
                onFreeze = onFreeze,
                onUnfreeze = onUnfreeze,
                onToggleSelected = onToggleSelected,
                onToggleFavorite = onToggleFavorite,
                onShowDetails = onShowDetails,
                onUninstall = onUninstall,
            )
            TabDestination.Settings -> SettingsScreen(
                contentPadding = innerPadding,
                selectedCount = uiState.selectedApps.size,
                rootAvailable = uiState.rootAvailable,
                biometricEnabled = uiState.biometricEnabled,
                onBiometricPreferenceChanged = onBiometricPreferenceChanged,
                onBeginPinReset = onBeginPinReset,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun QuickActionsPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    onFreezeAll: () -> Unit,
    onUnfreezeAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    text = { Text("Freeze All") },
                    icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                    onClick = onFreezeAll,
                )
                ExtendedFloatingActionButton(
                    text = { Text("Unfreeze All") },
                    icon = { Icon(Icons.Rounded.LockOpen, contentDescription = null) },
                    onClick = onUnfreezeAll,
                )
                ExtendedFloatingActionButton(
                    text = { Text("Refresh") },
                    icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                    onClick = onRefresh,
                )
            }
        }
        ExtendedFloatingActionButton(
            text = { Text(if (expanded) "Hide Options" else "Show Options") },
            icon = {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                )
            },
            onClick = onToggle,
        )
    }
}

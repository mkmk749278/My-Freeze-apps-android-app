package com.mkmk749278.myapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkmk749278.myapps.ui.MyAppsRoot
import com.mkmk749278.myapps.ui.theme.MyAppsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppsTheme {
                val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(application))
                val uiState by mainViewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        mainViewModel.clearStatusMessage()
                    }
                }

                MyAppsRoot(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onSelectTab = mainViewModel::selectTab,
                    onRefresh = mainViewModel::refreshAll,
                    onLaunch = { packageName -> mainViewModel.launchSelectedApp(packageName) },
                    onFreeze = mainViewModel::freezeApp,
                    onUnfreeze = mainViewModel::unfreezeApp,
                    onToggleSelected = mainViewModel::toggleAppSelection,
                    onToggleFavorite = mainViewModel::toggleFavorite,
                    onToggleQuickActions = mainViewModel::toggleQuickActions,
                    onFreezeAll = mainViewModel::freezeSelectedApps,
                    onUnfreezeAll = mainViewModel::unfreezeSelectedApps,
                    onCreatePin = mainViewModel::createPin,
                    onUnlockWithPin = mainViewModel::unlockWithPin,
                    onBiometricUnlock = mainViewModel::markUnlocked,
                    onBiometricPreferenceChanged = mainViewModel::setBiometricEnabled,
                    onBeginPinReset = mainViewModel::beginPinReset,
                    onShowDetails = { packageName ->
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            },
                        )
                    },
                    onUninstall = { packageName ->
                        context.startActivity(
                            Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:$packageName")
                            },
                        )
                    },
                )
            }
        }
    }
}

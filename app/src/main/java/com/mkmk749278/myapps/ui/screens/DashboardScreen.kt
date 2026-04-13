package com.mkmk749278.myapps.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.model.ManagedApp
import com.mkmk749278.myapps.ui.components.ManagedAppCard

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    selectedApps: List<ManagedApp>,
    favoriteApps: List<ManagedApp>,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    val remainingApps = selectedApps.filterNot { app -> favoriteApps.any { it.packageName == app.packageName } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (selectedApps.isEmpty()) {
            item {
                EmptyDashboardMessage()
            }
        } else {
            if (favoriteApps.isNotEmpty()) {
                item {
                    SectionHeader("Favorites")
                }
                items(favoriteApps, key = { it.packageName }) { app ->
                    ManagedAppCard(
                        app = app,
                        onLaunch = onLaunch,
                        onFreeze = onFreeze,
                        onUnfreeze = onUnfreeze,
                        onToggleFavorite = onToggleFavorite,
                        onShowDetails = onShowDetails,
                        onUninstall = onUninstall,
                    )
                }
            }
            item {
                SectionHeader("App Library")
            }
            items(remainingApps, key = { it.packageName }) { app ->
                ManagedAppCard(
                    app = app,
                    onLaunch = onLaunch,
                    onFreeze = onFreeze,
                    onUnfreeze = onUnfreeze,
                    onToggleFavorite = onToggleFavorite,
                    onShowDetails = onShowDetails,
                    onUninstall = onUninstall,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun EmptyDashboardMessage() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Your dashboard is empty.", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Open App Library and add the apps you want available for one-tap unfreeze and launch.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

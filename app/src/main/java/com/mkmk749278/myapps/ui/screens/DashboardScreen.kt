package com.mkmk749278.myapps.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.data.AppIconCache
import com.mkmk749278.myapps.model.AppCategoryFilter
import com.mkmk749278.myapps.model.ManagedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    apps: List<ManagedApp>,
    query: String,
    filter: AppCategoryFilter,
    optionsVisible: Boolean,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (AppCategoryFilter) -> Unit,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    var selectedApp by remember { mutableStateOf<ManagedApp?>(null) }
    val filteredApps = remember(apps, query, filter) {
        apps.filter { app ->
            val matchesFilter = when (filter) {
                AppCategoryFilter.All -> true
                AppCategoryFilter.User -> !app.isSystemApp
                AppCategoryFilter.System -> app.isSystemApp
            }
            val matchesQuery = query.isBlank() ||
                app.label.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    selectedApp?.let { currentApp ->
        DashboardOptionsSheet(
            app = currentApp,
            onDismiss = { selectedApp = null },
            onFreeze = onFreeze,
            onUnfreeze = onUnfreeze,
            onShowDetails = onShowDetails,
            onUninstall = onUninstall,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search dashboard") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCategoryFilter.entries.forEach { entry ->
                FilterChip(
                    selected = filter == entry,
                    onClick = { onFilterSelected(entry) },
                    label = { Text(entry.label) },
                )
            }
        }
        AnimatedContent(targetState = filteredApps.isEmpty(), label = "dashboard-empty") { empty ->
            if (empty) {
                EmptyDashboardMessage(
                    message = if (apps.isEmpty()) {
                        "Select apps in Library to build your launch dashboard."
                    } else {
                        "No dashboard apps match the current search or filter."
                    },
                )
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 92.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        DashboardAppTile(
                            app = app,
                            optionsVisible = optionsVisible,
                            onLaunch = onLaunch,
                            onShowOptions = { selectedApp = app },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardAppTile(
    app: ManagedApp,
    optionsVisible: Boolean,
    onLaunch: (String) -> Unit,
    onShowOptions: () -> Unit,
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) { AppIconCache.load(context, app.packageName) }

    Card(
        modifier = Modifier
            .animateItem()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { onLaunch(app.packageName) },
                onLongClick = onShowOptions,
            ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                )
            }
            if (app.isFrozen) {
                Text(
                    text = "Frozen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            if (optionsVisible) {
                IconButton(
                    onClick = onShowOptions,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f), CircleShape),
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "App options")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardOptionsSheet(
    app: ManagedApp,
    onDismiss: () -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = app.label, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OptionActionRow(label = if (app.isFrozen) "Freeze Again" else "Freeze", icon = Icons.Rounded.Lock) {
                onDismiss()
                onFreeze(app.packageName)
            }
            OptionActionRow(label = "Unfreeze", icon = Icons.Rounded.LockOpen) {
                onDismiss()
                onUnfreeze(app.packageName)
            }
            OptionActionRow(label = "App Info", icon = Icons.Rounded.Info) {
                onDismiss()
                onShowDetails(app.packageName)
            }
            OptionActionRow(label = "Uninstall", icon = Icons.Rounded.Delete) {
                onDismiss()
                onUninstall(app.packageName)
            }
        }
    }
}

@Composable
private fun OptionActionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun EmptyDashboardMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

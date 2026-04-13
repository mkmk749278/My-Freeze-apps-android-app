package com.mkmk749278.myapps.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.model.ManagedApp
import com.mkmk749278.myapps.ui.components.ManagedAppCard

@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    apps: List<ManagedApp>,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onToggleSelected: (String, Boolean) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = apps.filter {
        query.isBlank() || it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search installed apps") },
                modifier = Modifier.fillParentMaxWidth(),
            )
        }
        items(filteredApps, key = { it.packageName }) { app ->
            ManagedAppCard(
                app = app,
                showSelectionControl = true,
                onLaunch = onLaunch,
                onFreeze = onFreeze,
                onUnfreeze = onUnfreeze,
                onToggleSelected = onToggleSelected,
                onToggleFavorite = onToggleFavorite,
                onShowDetails = onShowDetails,
                onUninstall = onUninstall,
            )
        }
    }
}

package com.mkmk749278.myapps.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.data.AppIconCache
import com.mkmk749278.myapps.model.ManagedApp

@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    apps: List<ManagedApp>,
    onToggleSelected: (String, Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Select the apps that should appear in the dashboard grid.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(apps, key = { it.packageName }) { app ->
            LibraryAppRow(app = app, onToggleSelected = onToggleSelected)
        }
    }
}

@Composable
private fun LibraryAppRow(
    app: ManagedApp,
    onToggleSelected: (String, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) { AppIconCache.load(context, app.packageName) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleSelected(app.packageName, !app.isSelected) },
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Image(bitmap = icon, contentDescription = app.label, modifier = Modifier.size(44.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = app.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = buildString {
                        append(if (app.isSystemApp) "System" else "User")
                        append(" • ")
                        append(if (app.isFrozen) "Frozen" else "Ready")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = { onToggleSelected(app.packageName, !app.isSelected) },
                label = { Text(if (app.isSelected) "Added" else "Add") },
            )
        }
    }
}

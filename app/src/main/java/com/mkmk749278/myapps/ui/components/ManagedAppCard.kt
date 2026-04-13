package com.mkmk749278.myapps.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.mkmk749278.myapps.model.ManagedApp
import androidx.compose.foundation.Image

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManagedAppCard(
    app: ManagedApp,
    modifier: Modifier = Modifier,
    showSelectionControl: Boolean = false,
    onLaunch: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit,
    onToggleSelected: ((String, Boolean) -> Unit)? = null,
    onToggleFavorite: (String, Boolean) -> Unit,
    onShowDetails: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val packageManager = context.packageManager
    val iconBitmap = remember(app.packageName) {
        runCatching { packageManager.getApplicationIcon(app.packageName).toBitmap(96, 96).asImageBitmap() }
            .getOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (app.isLaunchable) onLaunch(app.packageName) else menuExpanded = true },
                onLongClick = { menuExpanded = true },
            ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(52.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = app.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = buildString {
                        append(if (app.isSystemApp) "System" else "User")
                        append(" • ")
                        append(if (app.isFrozen) "Frozen" else "Ready")
                        append(" • ")
                        append(app.packageName)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showSelectionControl) {
                    AssistChip(
                        onClick = { onToggleSelected?.invoke(app.packageName, !app.isSelected) },
                        label = { Text(if (app.isSelected) "In Dashboard" else "Add to Dashboard") },
                    )
                }
            }
            IconButton(onClick = { onToggleFavorite(app.packageName, !app.isFavorite) }) {
                Icon(
                    imageVector = if (app.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (app.isFavorite) "Remove favorite" else "Favorite app",
                )
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Open menu")
            }
        }
    }

    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(
            text = { Text("Launch") },
            onClick = {
                menuExpanded = false
                onLaunch(app.packageName)
            },
            enabled = app.isLaunchable,
        )
        DropdownMenuItem(
            text = { Text("Freeze") },
            onClick = {
                menuExpanded = false
                onFreeze(app.packageName)
            },
        )
        DropdownMenuItem(
            text = { Text("Unfreeze") },
            onClick = {
                menuExpanded = false
                onUnfreeze(app.packageName)
            },
        )
        DropdownMenuItem(
            text = { Text(if (app.isSelected) "Remove from Dashboard" else "Add to Dashboard") },
            onClick = {
                menuExpanded = false
                onToggleSelected?.invoke(app.packageName, !app.isSelected)
            },
            enabled = onToggleSelected != null,
        )
        DropdownMenuItem(
            text = { Text("App Details") },
            onClick = {
                menuExpanded = false
                onShowDetails(app.packageName)
            },
        )
        DropdownMenuItem(
            text = { Text("Uninstall") },
            onClick = {
                menuExpanded = false
                onUninstall(app.packageName)
            },
        )
    }
}

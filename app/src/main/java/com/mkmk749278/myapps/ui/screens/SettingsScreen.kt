package com.mkmk749278.myapps.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    selectedCount: Int,
    rootAvailable: Boolean,
    biometricEnabled: Boolean,
    onBiometricPreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsCard(title = "Security") {
            Text(
                text = "A custom PIN protects the app shell, while biometric unlock can reuse the device credential for fast access.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = "Biometric Unlock",
                subtitle = "Allow fingerprint, face, or device credential on the lock screen.",
                checked = biometricEnabled,
                onCheckedChange = onBiometricPreferenceChanged,
            )
            Button(onClick = onBeginPinReset, modifier = Modifier.fillMaxWidth()) {
                Text("Change PIN")
            }
        }

        SettingsCard(title = "Runtime") {
            Text(text = "Dashboard apps selected: $selectedCount")
            Text(text = if (rootAvailable) "Root access is available." else "Root access could not be verified.")
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh Installed Apps")
            }
        }

        SettingsCard(title = "Production Notes") {
            Text(
                text = "App icon assets are vector placeholders sized for Android adaptive icons. Replace them with production artwork before distribution if desired.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable Column.() -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

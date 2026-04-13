package com.mkmk749278.myapps.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkmk749278.myapps.model.AuthMethodState
import com.mkmk749278.myapps.model.OperationBackend
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    selectedCount: Int,
    activeBackend: OperationBackend,
    rootAvailable: Boolean,
    shizukuAvailable: Boolean,
    shizukuPermissionGranted: Boolean,
    authMethods: AuthMethodState,
    onPinPreferenceChanged: (Boolean) -> Unit,
    onFingerprintPreferenceChanged: (Boolean) -> Unit,
    onFacePreferenceChanged: (Boolean) -> Unit,
    onBeginPinReset: () -> Unit,
    onRequestShizukuPermission: suspend () -> Unit,
    onRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsCard(title = "Authentication") {
            Text(
                text = "Choose which verification methods are active before opening the app shell.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = "PIN",
                subtitle = "Keep manual PIN verification available.",
                checked = authMethods.pinEnabled,
                onCheckedChange = onPinPreferenceChanged,
            )
            ToggleRow(
                title = "Fingerprint",
                subtitle = "Offer the system biometric prompt with screen-lock fallback.",
                checked = authMethods.fingerprintEnabled,
                onCheckedChange = onFingerprintPreferenceChanged,
            )
            ToggleRow(
                title = "Face Lock",
                subtitle = "Offer the same system biometric prompt for enrolled face unlock.",
                checked = authMethods.faceEnabled,
                onCheckedChange = onFacePreferenceChanged,
            )
            Button(onClick = onBeginPinReset, modifier = Modifier.fillMaxWidth()) {
                Text("Change PIN")
            }
        }

        SettingsCard(title = "Access Backend") {
            Text(text = "Active backend: ${activeBackend.label}")
            Text(text = if (rootAvailable) "Root access detected." else "Root access not detected.")
            Text(
                text = when {
                    !shizukuAvailable -> "Shizuku is not running or not installed."
                    shizukuPermissionGranted -> "Shizuku permission granted."
                    else -> "Shizuku detected but permission is still required."
                },
            )
            if (shizukuAvailable && !shizukuPermissionGranted) {
                Button(
                    onClick = { scope.launch { onRequestShizukuPermission() } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant Shizuku Access")
                }
            }
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh App Status")
            }
        }

        SettingsCard(title = "Dashboard") {
            Text(text = "Apps pinned to dashboard: $selectedCount")
            Text(
                text = "The home screen is data-driven so additional tabs can be introduced with minimal UI changes later.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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

package com.mkmk749278.myapps.ui.screens

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mkmk749278.myapps.model.AuthMethodState
import com.mkmk749278.myapps.model.LockMode

@Composable
fun LockScreen(
    lockMode: LockMode,
    authMethods: AuthMethodState,
    onCreatePin: (String, String) -> Unit,
    onUnlockWithPin: (String) -> Unit,
    onBiometricUnlock: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val context = LocalContext.current
    val biometricAvailable = remember(context) { canAuthenticate(context) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (lockMode == LockMode.Setup) "Secure My Apps" else "Unlock My Apps",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (lockMode == LockMode.Setup) {
                    "Create a secure PIN before enabling optional fingerprint or face unlock."
                } else {
                    "Verify with any enabled method to enter the dashboard."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            )
            if (lockMode == LockMode.Setup || authMethods.pinEnabled) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit) },
                    label = { Text(if (lockMode == LockMode.Setup) "Create PIN" else "PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
            if (lockMode == LockMode.Setup) {
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.filter(Char::isDigit) },
                    label = { Text("Confirm PIN") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
            if (lockMode == LockMode.Setup || authMethods.pinEnabled) {
                Button(
                    onClick = {
                        if (lockMode == LockMode.Setup) onCreatePin(pin, confirmation) else onUnlockWithPin(pin)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                ) {
                    Text(if (lockMode == LockMode.Setup) "Save PIN" else "Unlock with PIN")
                }
            }
            if (lockMode == LockMode.Locked && authMethods.hasBiometricOption) {
                if (biometricAvailable) {
                    if (authMethods.fingerprintEnabled) {
                        BiometricButton(
                            label = "Unlock with Fingerprint",
                            icon = { Icon(Icons.Rounded.Fingerprint, contentDescription = null) },
                            onClick = { showBiometricPrompt(context, onBiometricUnlock) },
                        )
                    }
                    if (authMethods.faceEnabled) {
                        BiometricButton(
                            label = "Unlock with Face Lock",
                            icon = { Icon(Icons.Rounded.Visibility, contentDescription = null) },
                            onClick = { showBiometricPrompt(context, onBiometricUnlock) },
                        )
                    }
                } else {
                    Text(
                        text = "Biometric unlock is enabled, but the device has no compatible enrolled biometric. Screen-lock credentials remain the system fallback when biometrics are available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        icon()
        Text(text = label, modifier = Modifier.padding(start = 12.dp))
    }
}

private fun showBiometricPrompt(context: Context, onBiometricUnlock: () -> Unit) {
    val activity = context as? FragmentActivity ?: return
    BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onBiometricUnlock()
            }
        },
    ).authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock My Apps")
            .setSubtitle("Use your enrolled biometric or screen-lock credential")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build(),
    )
}

private fun canAuthenticate(context: Context): Boolean {
    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    return BiometricManager.from(context).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
}

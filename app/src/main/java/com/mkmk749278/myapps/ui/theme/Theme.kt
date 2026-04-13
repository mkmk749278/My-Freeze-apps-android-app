package com.mkmk749278.myapps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MyAppsDarkColors = darkColorScheme(
    primary = SkyBlue,
    onPrimary = MidnightBlue,
    secondary = Frost,
    onSecondary = MidnightBlue,
    tertiary = Aurora,
    background = Night,
    surface = NightSurface,
    onSurface = Snow,
    onSurfaceVariant = Mist,
)

@Composable
fun MyAppsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyAppsDarkColors,
        typography = Typography,
        content = content,
    )
}

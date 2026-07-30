package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkPrimaryContainer,
    onPrimaryContainer = ElegantDarkOnPrimaryContainer,
    secondary = ElegantDarkSecondary,
    onSecondary = ElegantDarkOnSecondary,
    secondaryContainer = ElegantDarkSecondaryContainer,
    onSecondaryContainer = ElegantDarkOnSecondaryContainer,
    background = ElegantDarkBg,
    onBackground = ElegantDarkTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantDarkTextSecondary,
    outline = ElegantDarkOutline,
    error = ElegantDarkError,
    errorContainer = ElegantDarkErrorContainer,
    onErrorContainer = ElegantDarkOnErrorContainer
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = ElegantLightPrimary,
    onPrimary = ElegantLightOnPrimary,
    primaryContainer = ElegantLightPrimaryContainer,
    onPrimaryContainer = ElegantLightOnPrimaryContainer,
    secondary = ElegantLightSecondary,
    onSecondary = ElegantLightOnSecondary,
    secondaryContainer = ElegantLightSecondaryContainer,
    onSecondaryContainer = ElegantLightOnSecondaryContainer,
    background = ElegantLightBg,
    onBackground = ElegantLightTextPrimary,
    surface = ElegantLightSurface,
    onSurface = ElegantLightTextPrimary,
    surfaceVariant = ElegantLightSurfaceVariant,
    onSurfaceVariant = ElegantLightTextSecondary,
    outline = ElegantLightOutline,
    error = ElegantLightError,
    errorContainer = ElegantLightErrorContainer,
    onErrorContainer = ElegantLightOnErrorContainer
)

@Composable
fun RepairPosTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


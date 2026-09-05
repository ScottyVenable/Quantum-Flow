package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FleetLightColorScheme = lightColorScheme(
    primary = NotewareTextPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = NotewareBubbleAgentLight,
    onPrimaryContainer = NotewareTextPrimaryLight,
    secondary = Color(0xFF374151),
    onSecondary = Color.White,
    secondaryContainer = NotewareBubbleAgentLight,
    onSecondaryContainer = NotewareTextPrimaryLight,
    tertiary = TelemetryGreen,
    onTertiary = Color.White,
    background = NotewareBgLight,
    onBackground = NotewareTextPrimaryLight,
    surface = NotewareSurfaceLight,
    onSurface = NotewareTextPrimaryLight,
    surfaceVariant = NotewareBubbleAgentLight,
    onSurfaceVariant = NotewareTextSecondaryLight,
    outline = NotewareBorderLight,
    error = NotewareLogoutRed,
    onError = Color.White
)

private val FleetDarkColorScheme = darkColorScheme(
    primary = NotewareBubbleUserDark,
    onPrimary = Color.Black,
    primaryContainer = NotewareSurfaceDark,
    onPrimaryContainer = NotewareTextPrimaryDark,
    secondary = Color(0xFFD1D5DB),
    onSecondary = Color.Black,
    secondaryContainer = NotewareBubbleAgentDark,
    onSecondaryContainer = Color.White,
    tertiary = TelemetryGreen,
    onTertiary = Color.Black,
    background = NotewareBgDark,
    onBackground = NotewareTextPrimaryDark,
    surface = NotewareSurfaceDark,
    onSurface = NotewareTextPrimaryDark,
    surfaceVariant = NotewareBubbleAgentDark,
    onSurfaceVariant = NotewareTextSecondaryDark,
    outline = NotewareBorderDark,
    error = NotewareLogoutRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FleetDarkColorScheme else FleetLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AusPostColorScheme = darkColorScheme(
    primary = AusPostRed,
    onPrimary = Color.White,
    primaryContainer = AusPostRedDark,
    onPrimaryContainer = Color.White,
    secondary = RoundGreen,
    onSecondary = Color.Black,
    secondaryContainer = RoundGreenContainer,
    onSecondaryContainer = RoundGreen,
    tertiary = AusPostGold,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun AusPostTheme(
    darkTheme: Boolean = true, // Default to high-contrast dark theme for easy outdoor reading
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AusPostColorScheme,
        typography = Typography,
        content = content
    )
}


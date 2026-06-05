package com.corecoders.gymbuddy.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LiftRed,
    onPrimary = Color.White,
    primaryContainer = LiftRed.copy(alpha = 0.1f),
    onPrimaryContainer = LiftRed,
    
    background = LightBackground,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBackground,
    onSurfaceVariant = LightTextSecondary,
    
    outline = LightDivider,
    secondary = LightTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = LiftRed,
    onPrimary = Color.White,
    primaryContainer = LiftRed.copy(alpha = 0.2f),
    onPrimaryContainer = LiftRed,
    
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = DarkTextSecondary,
    
    outline = DarkDivider,
    secondary = DarkTextSecondary
)

@Composable
fun GymBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

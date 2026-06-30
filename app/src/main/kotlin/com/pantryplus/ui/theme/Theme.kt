package com.pantryplus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Green80,
    secondary = Orange40,
    secondaryContainer = Orange80,
    error = Red40
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    primaryContainer = Green40,
    secondary = Orange80,
    secondaryContainer = Orange40,
    error = Red40
)

@Composable
fun PantryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonPink,
    tertiary = DeepPurple,
    background = VyroDarkBackground,
    surface = VyroDarkSurface,
    onPrimary = VyroDarkBackground,
    onSecondary = VyroDarkBackground,
    onTertiary = VyroLightSurface,
    onBackground = VyroLightSurface,
    onSurface = VyroLightSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurple,
    secondary = NeonPink,
    tertiary = ElectricBlue,
    background = VyroLightBackground,
    surface = VyroLightSurface,
    onPrimary = VyroLightSurface,
    onSecondary = VyroLightSurface,
    onTertiary = VyroDarkBackground,
    onBackground = VyroDarkBackground,
    onSurface = VyroDarkBackground,
)

@Composable
fun VyroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

package com.example.cryptographer.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.cryptographer.setup.configs.theme.DarkBackground
import com.example.cryptographer.setup.configs.theme.DarkSurface
import com.example.cryptographer.setup.configs.theme.DarkSurfaceVariant
import com.example.cryptographer.setup.configs.theme.LightBackground
import com.example.cryptographer.setup.configs.theme.LightSurface
import com.example.cryptographer.setup.configs.theme.LightSurfaceVariant
import com.example.cryptographer.setup.configs.theme.Pink40
import com.example.cryptographer.setup.configs.theme.Pink80
import com.example.cryptographer.setup.configs.theme.Purple40
import com.example.cryptographer.setup.configs.theme.Purple80
import com.example.cryptographer.setup.configs.theme.PurpleGrey40
import com.example.cryptographer.setup.configs.theme.PurpleGrey80
import com.example.cryptographer.setup.configs.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
)

@Composable
fun CryptographerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Disabled by default to use custom background colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

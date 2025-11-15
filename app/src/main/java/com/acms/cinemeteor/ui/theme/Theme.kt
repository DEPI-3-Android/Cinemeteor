package com.acms.cinemeteor.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = red,
    onPrimary = dInvert,
    secondary = dInvert,
    tertiary = Third,
    background = dBackground,
    surface = dBackground,
    onSurface = dInvert
)

private val LightColorScheme = lightColorScheme(
    primary = red,
    onPrimary = dInvert,
    secondary = Invert,
    tertiary = dThird,
    background = Background,
    surface = Background,
    onSurface = Invert
)

@Composable
fun CinemeteorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background, // أو تقدر تستخدم colorScheme.background
            darkIcons = !darkTheme // لو الوضع فاتح يخلي الأيقونات غامقة
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.background,
            darkIcons = !darkTheme,
            navigationBarContrastEnforced = false
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


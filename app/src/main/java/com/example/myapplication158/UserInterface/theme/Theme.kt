package com.example.myapplication158.UserInterface.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    secondary = AccentOrange,
    tertiary = BorderOrange,
    background = IosBackgroundDark,
    surface = IosSurfaceDark,
    surfaceVariant = Color(0xFF2C2C2E), // כמו כרטיסיות באייפון במצב כהה
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = IosTextLight,
    onSurface = IosTextLight,
    onSurfaceVariant = IosTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrangeDark,
    secondary = PrimaryOrange,
    tertiary = AccentOrange,
    background = IosBackgroundLight,
    surface = IosSurfaceLight,
    surfaceVariant = Color(0xFFE5E5EA), // כמו רקעי אפור באייפון הבהיר
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = IosTextDark,
    onSurface = IosTextDark,
    onSurfaceVariant = IosTextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
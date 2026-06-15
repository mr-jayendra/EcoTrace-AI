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

private val DarkColorScheme =
  darkColorScheme(
    primary = EcoDarkPrimary,
    secondary = EcoDarkSecondary,
    tertiary = EcoDarkTertiary,
    background = EcoDarkBackground,
    surface = EcoDarkSurface,
    surfaceVariant = EcoDarkSurfaceVariant,
    onPrimary = EcoDarkBackground,
    onSecondary = EcoDarkBackground,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    primaryContainer = EcoDarkSurfaceVariant,
    secondaryContainer = EcoDarkSurfaceVariant,
    outline = SleekOutline.copy(alpha = 0.3f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekPrimary,
    primaryContainer = SleekPrimaryContainer,
    secondary = SleekSecondary,
    secondaryContainer = SleekSecondaryContainer,
    tertiary = SleekPrimary,
    background = SleekBackground,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SleekSecondaryContainer,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onPrimaryContainer = SleekPrimary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onSecondaryContainer = SleekOnBackground,
    onBackground = SleekOnBackground,
    onSurface = SleekOnBackground,
    onSurfaceVariant = SleekSecondary,
    outline = SleekOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to force the "Sleek Interface" branding colors
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

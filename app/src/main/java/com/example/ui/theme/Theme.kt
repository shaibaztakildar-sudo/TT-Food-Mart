package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = BrandRedPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFEBEE),
  onPrimaryContainer = BrandRedDark,
  secondary = BrandOrangeAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFF3E0),
  onSecondaryContainer = Color(0xFFE65100),
  tertiary = BrandYellowStar,
  background = SurfaceBackground,
  onBackground = TextPrimary,
  surface = SurfaceCard,
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFFF1F3F5),
  onSurfaceVariant = TextSecondary,
  outline = DividerColor,
  error = NonVegRed,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = BrandRedPrimary,
  onPrimary = Color.White,
  secondary = BrandOrangeAccent,
  onSecondary = Color.White,
  background = Color(0xFF121212),
  onBackground = Color(0xFFEEEEEE),
  surface = Color(0xFF1E1E1E),
  onSurface = Color(0xFFEEEEEE),
  surfaceVariant = Color(0xFF2C2C2C),
  onSurfaceVariant = Color(0xFFB0B0B0),
  outline = Color(0xFF3E3E3E),
  error = NonVegRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

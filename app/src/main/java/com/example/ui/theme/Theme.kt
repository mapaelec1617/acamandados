package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
  primary = AcaAzulConfianza,
  onPrimary = Color.White,
  primaryContainer = AcaAzulContainer,
  onPrimaryContainer = AcaOnAzulContainer,
  secondary = AcaTurquesa,
  onSecondary = Color(0xFF00382E),
  secondaryContainer = AcaTurquesaContainer,
  onSecondaryContainer = AcaOnTurquesaContainer,
  tertiary = AcaTurquesaDark,
  onTertiary = Color.White,
  background = AcaBackground,
  onBackground = AcaGrisEquilibrio,
  surface = AcaSurface,
  onSurface = AcaGrisEquilibrio,
  surfaceVariant = AcaSurfaceVariant,
  onSurfaceVariant = AcaGrisSubtle,
  outline = Color(0xFFCBD5E1),
  error = AcaCoralNotice
)

private val DarkColorScheme = darkColorScheme(
  primary = AcaTurquesa,
  onPrimary = Color(0xFF00382E),
  primaryContainer = AcaAzulDark,
  onPrimaryContainer = Color.White,
  secondary = AcaTurquesaLight,
  onSecondary = Color(0xFF00382E),
  secondaryContainer = Color(0xFF004D40),
  onSecondaryContainer = Color(0xFFD2FAF0),
  tertiary = AcaAzulLight,
  onTertiary = Color.White,
  background = Color(0xFF0F172A),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF1E293B),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF475569),
  error = Color(0xFFF87171)
)

/**
 * Tema principal de AcaMandados con soporte oficial de Dynamic Colors (Android 12+)
 * y modo oscuro nativo conforme a las guías de Material Design 3.
 */
@Composable
fun AcaMandadosTheme(
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
    content = content
  )
}


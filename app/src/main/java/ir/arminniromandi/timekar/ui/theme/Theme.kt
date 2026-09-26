package ir.arminniromandi.timekar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.arminniromandi.timekar.domain.ThemeMode
import ir.arminniromandi.timekar.domain.UserSettings

@Composable
fun ChronosTheme(
    settings: UserSettings = UserSettings(),
    content: @Composable () -> Unit
) {
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val primaryColor = Color(settings.accentColor.colorHex)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.35f),
            onPrimaryContainer = Color(0xFFEEEFFF),
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            surfaceVariant = SurfaceContainerHighestDark,
            onSurfaceVariant = OnSurfaceVariantDark,
            surfaceContainer = SurfaceContainerDark,
            surfaceContainerLow = SurfaceContainerLowDark,
            surfaceContainerLowest = SurfaceContainerLowestDark,
            surfaceContainerHigh = SurfaceContainerHighDark,
            surfaceContainerHighest = SurfaceContainerHighestDark,
            outline = OutlineDark,
            outlineVariant = OutlineVariantDark,
            secondary = SecondaryDark,
            onSecondary = Color(0xFF0D1C2E),
            secondaryContainer = SecondaryContainerDark,
            onSecondaryContainer = OnSecondaryContainerDark,
            tertiary = TertiaryDark,
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor,
            onPrimaryContainer = Color(0xFFEEEFFF),
            surface = SurfaceLight,
            onSurface = OnSurfaceLight,
            surfaceVariant = SurfaceContainerHighestLight,
            onSurfaceVariant = OnSurfaceVariantLight,
            surfaceContainer = SurfaceContainerLight,
            surfaceContainerLow = SurfaceContainerLowLight,
            surfaceContainerLowest = SurfaceContainerLowestLight,
            surfaceContainerHigh = SurfaceContainerHighLight,
            surfaceContainerHighest = SurfaceContainerHighestLight,
            outline = OutlineLight,
            outlineVariant = OutlineVariantLight,
            secondary = SecondaryLight,
            onSecondary = Color.White,
            secondaryContainer = SecondaryContainerLight,
            onSecondaryContainer = OnSecondaryContainerLight,
            tertiary = TertiaryLight,
            error = PriorityHighColor,
            errorContainer = PriorityHighContainer
        )
    }

    val layoutDirection = if (settings.language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    ChronosTheme(content = content)
}


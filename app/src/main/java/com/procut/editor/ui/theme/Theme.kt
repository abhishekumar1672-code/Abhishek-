package com.procut.editor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val MaterialDarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextPrimary,
    primaryContainer = AccentBlueSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkPanel,
    onSurface = TextPrimary,
    surfaceVariant = DarkPanelElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = DangerRed,
    onError = TextPrimary
)

object ProCutTheme {
    val colors: ProCutColors
        @Composable
        @ReadOnlyComposable
        get() = LocalProCutColors.current

    val typography: ProCutTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalProCutTypography.current

    val dimens: ProCutDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalProCutDimens.current
}

@Composable
fun ProCutTheme(
    colors: ProCutColors = ProCutColors(),
    typography: ProCutTypography = ProCutTypography(),
    dimens: ProCutDimens = ProCutDimens(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalProCutColors provides colors,
        LocalProCutTypography provides typography,
        LocalProCutDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = MaterialDarkColors,
            content = content
        )
    }
}

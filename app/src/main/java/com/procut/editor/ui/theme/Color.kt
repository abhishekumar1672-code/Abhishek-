package com.procut.editor.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val DarkPanel = Color(0xFF1A1A1A)
val DarkPanelElevated = Color(0xFF232323)
val DarkBorder = Color(0xFF2E2E2E)
val TextPrimary = Color(0xFFE6E6E6)
val TextSecondary = Color(0xFF8C8C8C)
val TextMuted = Color(0xFF555555)
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueHover = Color(0xFF2563EB)
val AccentBlueSubtle = Color(0xFF1D283A)
val DangerRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF10B981)

// Track and Clip Colors (neutral, professional)
val TrackBackground = Color(0xFF161616)
val VideoClipColor = Color(0xFF1E3A5F)
val VideoClipBorder = Color(0xFF2B5282)
val AudioClipColor = Color(0xFF144234)
val AudioClipBorder = Color(0xFF1F604D)
val TextClipColor = Color(0xFF3B274F)
val TextClipBorder = Color(0xFF573974)
val PlayheadColor = Color(0xFF3B82F6)

@Immutable
data class ProCutColors(
    val background: Color = DarkBackground,
    val panel: Color = DarkPanel,
    val panelElevated: Color = DarkPanelElevated,
    val border: Color = DarkBorder,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val accent: Color = AccentBlue,
    val accentSubtle: Color = AccentBlueSubtle,
    val danger: Color = DangerRed,
    val warning: Color = WarningAmber,
    val success: Color = SuccessGreen,
    val trackBg: Color = TrackBackground,
    val videoClip: Color = VideoClipColor,
    val videoClipBorder: Color = VideoClipBorder,
    val audioClip: Color = AudioClipColor,
    val audioClipBorder: Color = AudioClipBorder,
    val textClip: Color = TextClipColor,
    val textClipBorder: Color = TextClipBorder,
    val playhead: Color = PlayheadColor
)

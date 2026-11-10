package com.procut.editor.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ProCutDimens(
    val spacingXs: Dp = 4.dp,
    val spacingSm: Dp = 8.dp,
    val spacingMd: Dp = 12.dp,
    val spacingLg: Dp = 16.dp,
    val spacingXl: Dp = 24.dp,

    val borderWidth: Dp = 1.dp,
    val borderFocusWidth: Dp = 1.5.dp,
    val cornerRadius: Dp = 4.dp,
    val cornerRadiusSm: Dp = 2.dp,

    val minTouchTarget: Dp = 44.dp,
    val iconSizeSm: Dp = 16.dp,
    val iconSize: Dp = 20.dp,
    val iconSizeLg: Dp = 24.dp,

    val topBarHeight: Dp = 44.dp,
    val transportBarHeight: Dp = 40.dp,
    val bottomTabsHeight: Dp = 56.dp,
    val trackHeaderWidth: Dp = 60.dp,
    val defaultTrackHeight: Dp = 48.dp,
    val playheadLineWidth: Dp = 1.5.dp,
    val playheadCapSize: Dp = 10.dp
)

val LocalProCutDimens = androidx.compose.runtime.staticCompositionLocalOf { ProCutDimens() }

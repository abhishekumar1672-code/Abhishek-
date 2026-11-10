package com.procut.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.procut.editor.ui.theme.ProCutTheme

@Composable
fun ProIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = false,
    tint: Color? = null,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens

    val resolvedTint = when {
        !enabled -> colors.textMuted
        isActive -> colors.accent
        tint != null -> tint
        else -> colors.textPrimary
    }

    val bgModifier = if (isActive) {
        Modifier.background(colors.accentSubtle, RoundedCornerShape(dimens.cornerRadius))
    } else Modifier

    val borderModifier = if (isActive) {
        Modifier.border(dimens.borderWidth, colors.accent, RoundedCornerShape(dimens.cornerRadius))
    } else Modifier

    Box(
        modifier = modifier
            .sizeIn(minWidth = dimens.minTouchTarget, minHeight = dimens.minTouchTarget)
            .then(bgModifier)
            .then(borderModifier)
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(dimens.iconSize),
            tint = resolvedTint
        )
    }
}

@Composable
fun ProToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val bgColor = if (isActive) colors.accentSubtle else colors.panel
    val borderColor = if (isActive) colors.accent else colors.border
    val contentColor = when {
        !enabled -> colors.textMuted
        isActive -> colors.accent
        else -> colors.textPrimary
    }

    Row(
        modifier = modifier
            .sizeIn(minHeight = dimens.minTouchTarget)
            .background(bgColor, RoundedCornerShape(dimens.cornerRadius))
            .border(dimens.borderWidth, borderColor, RoundedCornerShape(dimens.cornerRadius))
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(dimens.iconSizeSm),
            tint = contentColor
        )
        Text(
            text = label,
            style = typography.button,
            color = contentColor
        )
    }
}

@Preview(name = "ProIconButton Preview")
@Composable
fun PreviewProIconButton() {
    ProCutTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProIconButton(
                icon = Icons.Default.PlayArrow,
                contentDescription = "Play",
                onClick = {}
            )
            ProIconButton(
                icon = Icons.Outlined.ContentCut,
                contentDescription = "Cut",
                isActive = true,
                onClick = {}
            )
        }
    }
}

@Preview(name = "ProToolButton Preview")
@Composable
fun PreviewProToolButton() {
    ProCutTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProToolButton(
                icon = Icons.Outlined.ContentCut,
                label = "Razor Cut",
                isActive = false,
                onClick = {}
            )
            ProToolButton(
                icon = Icons.Outlined.ContentCut,
                label = "Razor Cut (Active)",
                isActive = true,
                onClick = {}
            )
        }
    }
}

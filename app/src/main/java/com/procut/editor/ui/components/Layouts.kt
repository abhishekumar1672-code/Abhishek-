package com.procut.editor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.procut.editor.ui.theme.ProCutTheme

@Composable
fun ProPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    cornerRadius: Dp? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens

    val bg = backgroundColor ?: colors.panel
    val border = borderColor ?: colors.border
    val radius = cornerRadius ?: dimens.cornerRadius

    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(radius))
            .border(dimens.borderWidth, border, RoundedCornerShape(radius))
            .clip(RoundedCornerShape(radius))
    ) {
        content()
    }
}

@Composable
fun ProDivider(
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    color: Color? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val resolvedColor = color ?: colors.border

    if (isVertical) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(dimens.borderWidth)
                .background(resolvedColor)
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(dimens.borderWidth)
                .background(resolvedColor)
        )
    }
}

@Composable
fun <T> ProTabBar(
    tabs: List<T>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    tabTitleProvider: (T) -> String,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier)
            .background(colors.panel)
            .border(dimens.borderWidth, colors.border)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        horizontalArrangement = if (scrollable) Arrangement.Start else Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tabTitleProvider(tab),
                    style = typography.button,
                    color = if (isSelected) colors.accent else colors.textSecondary
                )
                Spacer(modifier = Modifier.height(dimens.spacingXs))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(if (isSelected) 24.dp else 0.dp)
                        .background(if (isSelected) colors.accent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun ProSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = typography.sectionHeader,
            color = colors.textSecondary
        )
        if (action != null) {
            action()
        }
    }
}

enum class SheetSnapState {
    COLLAPSED,
    COMPACT,
    EXPANDED
}

@Composable
fun ProBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    snapState: SheetSnapState = SheetSnapState.COMPACT,
    onSnapStateChange: ((SheetSnapState) -> Unit)? = null,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val targetHeight = when (snapState) {
        SheetSnapState.COLLAPSED -> 0.dp
        SheetSnapState.COMPACT -> 260.dp
        SheetSnapState.EXPANDED -> 420.dp
    }
    val animatedHeight by animateDpAsState(targetValue = targetHeight, label = "sheetHeight")

    AnimatedVisibility(
        visible = visible && snapState != SheetSnapState.COLLAPSED,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .background(
                    colors.panel,
                    RoundedCornerShape(topStart = dimens.cornerRadius, topEnd = dimens.cornerRadius)
                )
                .border(
                    dimens.borderWidth,
                    colors.border,
                    RoundedCornerShape(topStart = dimens.cornerRadius, topEnd = dimens.cornerRadius)
                )
                .padding(dimens.spacingSm)
        ) {
            // Drag handle with snap toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clickable {
                        val nextState = when (snapState) {
                            SheetSnapState.COMPACT -> SheetSnapState.EXPANDED
                            SheetSnapState.EXPANDED -> SheetSnapState.COMPACT
                            SheetSnapState.COLLAPSED -> SheetSnapState.COMPACT
                        }
                        onSnapStateChange?.invoke(nextState)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(colors.border, RoundedCornerShape(2.dp))
                )
            }

            // Optional Header Row
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = typography.title,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onDismissRequest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                ProDivider(modifier = Modifier.padding(vertical = dimens.spacingXs))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(dimens.spacingXs)
            ) {
                content()
            }
        }
    }
}

@Preview(name = "Layouts Preview")
@Composable
fun PreviewLayouts() {
    ProCutTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProPanel(modifier = Modifier.padding(8.dp)) {
                Text("This is a ProPanel with 1dp border and 4dp radius", modifier = Modifier.padding(12.dp))
            }
            ProSectionHeader(title = "Transform Controls")
            ProTabBar(
                tabs = listOf("Edit", "Audio", "Color", "Effects"),
                selectedTab = "Edit",
                onTabSelected = {},
                tabTitleProvider = { it }
            )
            ProDivider()
        }
    }
}

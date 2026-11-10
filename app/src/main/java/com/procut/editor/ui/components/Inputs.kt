package com.procut.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.procut.editor.ui.theme.ProCutTheme
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    min: Float = 0f,
    max: Float = 100f,
    defaultValue: Float = 0f,
    step: Float? = null,
    unit: String = "",
    enabled: Boolean = true,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (enabled) onValueChange(defaultValue)
                    }
                )
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style = typography.caption,
                    color = if (enabled) colors.textSecondary else colors.textMuted
                )
            }
            val formattedVal = if (unit.isNotEmpty()) {
                String.format(Locale.US, "%.1f %s", value, unit)
            } else {
                String.format(Locale.US, "%.1f", value)
            }
            Text(
                text = formattedVal,
                style = typography.numericValue,
                color = if (enabled) colors.textPrimary else colors.textMuted
            )
        }

        val stepsCount = if (step != null && step > 0f) {
            ((max - min) / step).roundToInt() - 1
        } else 0

        Slider(
            value = value.coerceIn(min, max),
            onValueChange = onValueChange,
            valueRange = min..max,
            steps = stepsCount.coerceAtLeast(0),
            enabled = enabled,
            modifier = Modifier.height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.border,
                disabledThumbColor = colors.textMuted,
                disabledActiveTrackColor = colors.textMuted,
                disabledInactiveTrackColor = colors.border
            )
        )
    }
}

@Composable
fun ProNumberField(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    unit: String = "",
    min: Float = -10000f,
    max: Float = 10000f,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography
    var showDialog by remember { mutableStateOf(false) }
    var textInput by remember(value) { mutableStateOf(String.format(Locale.US, "%.1f", value)) }

    Row(
        modifier = modifier
            .background(colors.panel, RoundedCornerShape(dimens.cornerRadius))
            .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .clickable(onClick = { showDialog = true })
            .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = typography.caption,
                color = colors.textSecondary
            )
        }
        Text(
            text = "${String.format(Locale.US, "%.1f", value)}${if (unit.isNotEmpty()) " $unit" else ""}",
            style = typography.numericValue,
            color = colors.accent
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = label ?: "Edit Value",
                    style = typography.title,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                    BasicTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        textStyle = typography.numericValue.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                textInput.toFloatOrNull()?.let {
                                    onValueChange(it.coerceIn(min, max))
                                }
                                showDialog = false
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadius))
                            .border(dimens.borderWidth, colors.accent, RoundedCornerShape(dimens.cornerRadius))
                            .padding(dimens.spacingSm)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        textInput.toFloatOrNull()?.let {
                            onValueChange(it.coerceIn(min, max))
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK", style = typography.button, color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", style = typography.button, color = colors.textSecondary)
                }
            },
            containerColor = colors.panel,
            shape = RoundedCornerShape(dimens.cornerRadius)
        )
    }
}

@Composable
fun ProToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Row(
        modifier = modifier
            .sizeIn(minHeight = dimens.minTouchTarget)
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = dimens.spacingXs)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = typography.bodySmall,
                color = if (enabled) colors.textPrimary else colors.textMuted
            )
        }

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(20.dp)
                .background(
                    if (checked) colors.accent else colors.panelElevated,
                    RoundedCornerShape(10.dp)
                )
                .border(
                    dimens.borderWidth,
                    if (checked) colors.accent else colors.border,
                    RoundedCornerShape(10.dp)
                )
                .padding(2.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(if (checked) colors.textPrimary else colors.textSecondary, CircleShape)
            )
        }
    }
}

@Composable
fun <T> ProDropdown(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .sizeIn(minHeight = dimens.minTouchTarget)
                .background(colors.panel, RoundedCornerShape(dimens.cornerRadius))
                .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                .clip(RoundedCornerShape(dimens.cornerRadius))
                .clickable(onClick = { expanded = true })
                .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
            Text(
                text = labelProvider(selectedItem),
                style = typography.button,
                color = colors.textPrimary
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                modifier = Modifier.size(dimens.iconSizeSm),
                tint = colors.textSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.panelElevated)
                .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = labelProvider(item),
                                style = typography.bodySmall,
                                color = if (isSelected) colors.accent else colors.textPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(dimens.iconSizeSm),
                                    tint = colors.accent
                                )
                            }
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProColorSwatch(
    color: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String? = null
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography
    var showPalette by remember { mutableStateOf(false) }

    val presetColors = listOf(
        Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFFEF4444),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF3B82F6),
        Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF6B7280)
    )

    Row(
        modifier = modifier
            .clickable(onClick = { showPalette = !showPalette })
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = typography.caption,
                color = colors.textSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, RoundedCornerShape(dimens.cornerRadiusSm))
                .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm))
        )
    }

    if (showPalette) {
        AlertDialog(
            onDismissRequest = { showPalette = false },
            title = {
                Text(
                    text = label ?: "Select Color",
                    style = typography.title,
                    color = colors.textPrimary
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(preset, RoundedCornerShape(4.dp))
                                .border(
                                    if (preset == color) 2.dp else 1.dp,
                                    if (preset == color) colors.accent else colors.border,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    onColorSelected(preset)
                                    showPalette = false
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            containerColor = colors.panel,
            shape = RoundedCornerShape(dimens.cornerRadius)
        )
    }
}

@Preview(name = "Inputs Preview")
@Composable
fun PreviewInputs() {
    ProCutTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProSlider(
                value = 45f,
                onValueChange = {},
                label = "Opacity",
                unit = "%",
                min = 0f,
                max = 100f
            )
            ProNumberField(
                value = 1920f,
                onValueChange = {},
                label = "Width",
                unit = "px"
            )
            ProToggle(
                checked = true,
                onCheckedChange = {},
                label = "Safe-Zone Guides"
            )
            ProDropdown(
                items = listOf("16:9", "9:16", "1:1", "4:5"),
                selectedItem = "16:9",
                onItemSelected = {},
                labelProvider = { it },
                label = "Aspect Ratio"
            )
            ProColorSwatch(
                color = Color(0xFF3B82F6),
                onColorSelected = {},
                label = "Solid Color"
            )
        }
    }
}

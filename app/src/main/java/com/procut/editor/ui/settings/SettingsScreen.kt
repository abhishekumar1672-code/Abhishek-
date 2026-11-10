package com.procut.editor.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.procut.editor.ui.components.ProDivider
import com.procut.editor.ui.components.ProIconButton
import com.procut.editor.ui.components.ProPanel
import com.procut.editor.ui.components.ProSectionHeader
import com.procut.editor.ui.components.ProToggle
import com.procut.editor.ui.theme.ProCutTheme
import com.procut.editor.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val lowPowerMode by viewModel.lowPowerMode.collectAsState()
    val pixabayKey by viewModel.pixabayApiKey.collectAsState()
    val freesoundKey by viewModel.freesoundApiKey.collectAsState()
    val jamendoKey by viewModel.jamendoApiKey.collectAsState()

    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 600

    var showClearConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.panel)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.topBarHeight)
                        .padding(horizontal = dimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack,
                        testTag = "settings_back_btn"
                    )
                    Spacer(modifier = Modifier.width(dimens.spacingSm))
                    Text(
                        text = "Settings",
                        style = typography.title.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    if (isTabletOrLandscape) {
                        Spacer(modifier = Modifier.width(dimens.spacingSm))
                        Box(
                            modifier = Modifier
                                .background(colors.accentSubtle, RoundedCornerShape(dimens.cornerRadiusSm))
                                .border(dimens.borderWidth, colors.accent, RoundedCornerShape(dimens.cornerRadiusSm))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "WORKSTATION MODE",
                                style = typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = colors.accent
                            )
                        }
                    }
                }
                ProDivider()
            }
        }
    ) { innerPadding ->
        if (isTabletOrLandscape) {
            // Dual-column tablet workstation layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(dimens.spacingMd),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingLg)
            ) {
                // Left Column: Performance & Cache
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingLg)
                ) {
                    PerformanceSection(
                        lowPowerMode = lowPowerMode,
                        onLowPowerModeChange = { viewModel.setLowPowerMode(it) }
                    )
                    StorageSection()
                }

                // Right Column: Encrypted API keys & About
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingLg)
                ) {
                    ApiKeysSection(
                        pixabayKey = pixabayKey,
                        onPixabayKeySave = { viewModel.setPixabayKey(it) },
                        freesoundKey = freesoundKey,
                        onFreesoundKeySave = { viewModel.setFreesoundKey(it) },
                        jamendoKey = jamendoKey,
                        onJamendoKeySave = { viewModel.setJamendoKey(it) },
                        onClearClick = { showClearConfirmation = true }
                    )
                    AboutSection()
                }
            }
        } else {
            // Standard single-column mobile layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingLg)
            ) {
                PerformanceSection(
                    lowPowerMode = lowPowerMode,
                    onLowPowerModeChange = { viewModel.setLowPowerMode(it) }
                )
                ApiKeysSection(
                    pixabayKey = pixabayKey,
                    onPixabayKeySave = { viewModel.setPixabayKey(it) },
                    freesoundKey = freesoundKey,
                    onFreesoundKeySave = { viewModel.setFreesoundKey(it) },
                    jamendoKey = jamendoKey,
                    onJamendoKeySave = { viewModel.setJamendoKey(it) },
                    onClearClick = { showClearConfirmation = true }
                )
                StorageSection()
                AboutSection()
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Stored API Keys?", style = typography.title, color = colors.danger) },
            text = {
                Text(
                    "All stored Pixabay, Freesound, and Jamendo API keys will be securely deleted from the device keystore.",
                    style = typography.bodySmall,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllKeys()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Text("Clear All", style = typography.button)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", style = typography.button, color = colors.textSecondary)
                }
            },
            containerColor = colors.panel,
            shape = RoundedCornerShape(dimens.cornerRadius)
        )
    }
}

@Composable
private fun PerformanceSection(
    lowPowerMode: Boolean,
    onLowPowerModeChange: (Boolean) -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
        ProSectionHeader(title = "Performance & Quality")
        ProPanel(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimens.spacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Low-Power Mode",
                                style = typography.body,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Renders timeline preview at 1/4 resolution for low-end devices and smoother scrubbing.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }
                    ProToggle(
                        checked = lowPowerMode,
                        onCheckedChange = onLowPowerModeChange,
                        testTag = "settings_low_power_toggle"
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeysSection(
    pixabayKey: String,
    onPixabayKeySave: (String) -> Unit,
    freesoundKey: String,
    onFreesoundKeySave: (String) -> Unit,
    jamendoKey: String,
    onJamendoKeySave: (String) -> Unit,
    onClearClick: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
        ProSectionHeader(
            title = "External Media Services (Encrypted)",
            action = {
                TextButton(
                    onClick = onClearClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Clear Keys", style = typography.caption, color = colors.danger)
                }
            }
        )
        ProPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMd)
            ) {
                SecureApiKeyField(
                    label = "Pixabay API Key (Stock Images & 4K Video)",
                    value = pixabayKey,
                    onSave = onPixabayKeySave,
                    testTag = "api_key_pixabay"
                )

                ProDivider()

                SecureApiKeyField(
                    label = "Freesound API Key (SFX & Sound Library)",
                    value = freesoundKey,
                    onSave = onFreesoundKeySave,
                    testTag = "api_key_freesound"
                )

                ProDivider()

                SecureApiKeyField(
                    label = "Jamendo API Key (Royalty-Free Music)",
                    value = jamendoKey,
                    onSave = onJamendoKeySave,
                    testTag = "api_key_jamendo"
                )
            }
        }
    }
}

@Composable
private fun StorageSection() {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
        ProSectionHeader(title = "Storage & Cache Management")
        ProPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Timeline Render Cache", style = typography.body, color = colors.textPrimary)
                            Text("0 MB used", style = typography.caption, color = colors.textSecondary)
                        }
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.panelElevated,
                            contentColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(dimens.cornerRadius),
                        modifier = Modifier
                            .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                            .height(32.dp)
                    ) {
                        Text("Clear", style = typography.button)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection() {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
        ProSectionHeader(title = "About ProCut")
        ProPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Text("ProCut Mobile & Tablet Workstation", style = typography.body, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Engine: OpenGL ES 3.0 / GLSL Hardware Shaders", style = typography.caption, color = colors.textSecondary)
                Text("• Pipeline: Media3 ExoPlayer & MediaCodec Acceleration", style = typography.caption, color = colors.textSecondary)
                Text("• Precision: Microsecond TimeUs Timeline (Zero Floating Point Drift)", style = typography.caption, color = colors.textSecondary)
                Text("• Storage: AES-256 GCM Encrypted Keystore & Room DB", style = typography.caption, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun SecureApiKeyField(
    label: String,
    value: String,
    onSave: (String) -> Unit,
    testTag: String
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    var currentText by remember(value) { mutableStateOf(value) }
    var isVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
        Text(text = label, style = typography.caption, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadius))
                    .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                    .padding(horizontal = dimens.spacingSm, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = currentText,
                    onValueChange = {
                        currentText = it
                        onSave(it)
                    },
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = typography.numericValue.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
                if (currentText.isEmpty()) {
                    Text(
                        text = "Paste API key...",
                        style = typography.caption,
                        color = colors.textMuted
                    )
                }
            }

            ProIconButton(
                icon = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isVisible) "Hide" else "Show",
                onClick = { isVisible = !isVisible },
                testTag = "${testTag}_visibility"
            )

            if (currentText.isNotEmpty()) {
                ProIconButton(
                    icon = Icons.Default.Key,
                    contentDescription = "Clear",
                    onClick = {
                        currentText = ""
                        onSave("")
                    },
                    tint = colors.danger,
                    testTag = "${testTag}_clear"
                )
            }
        }
    }
}

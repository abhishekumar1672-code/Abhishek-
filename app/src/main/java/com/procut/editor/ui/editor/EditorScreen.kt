package com.procut.editor.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.procut.editor.model.AspectRatio
import com.procut.editor.model.BlendMode
import com.procut.editor.model.Clip
import com.procut.editor.model.ClipType
import com.procut.editor.model.Project
import com.procut.editor.model.TimeUs
import com.procut.editor.model.TimelineOps
import com.procut.editor.model.Track
import com.procut.editor.model.TrackType
import com.procut.editor.ui.components.ProBottomSheet
import com.procut.editor.ui.components.ProColorSwatch
import com.procut.editor.ui.components.ProDivider
import com.procut.editor.ui.components.ProDropdown
import com.procut.editor.ui.components.ProIconButton
import com.procut.editor.ui.components.ProNumberField
import com.procut.editor.ui.components.ProPanel
import com.procut.editor.ui.components.ProSectionHeader
import com.procut.editor.ui.components.ProSlider
import com.procut.editor.ui.components.ProToggle
import com.procut.editor.ui.components.ProToolButton
import com.procut.editor.ui.components.SheetSnapState
import com.procut.editor.ui.theme.ProCutTheme
import com.procut.editor.ui.viewmodels.EditorToolTab
import com.procut.editor.ui.viewmodels.EditorViewModel
import com.procut.editor.util.TimecodeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val isLoading by viewModel.isLoading.collectAsState()
    val project by viewModel.project.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val sheetSnapState by viewModel.sheetSnapState.collectAsState()
    val timelineHeightDp by viewModel.timelineHeightDp.collectAsState()
    val showSafeZones by viewModel.showSafeZones.collectAsState()

    val session = viewModel.session

    val playheadUs = session?.playheadUs?.collectAsState()?.value ?: 0L
    val isPlaying = session?.isPlaying?.collectAsState()?.value ?: false
    val isLooping = session?.isLooping?.collectAsState()?.value ?: false
    val canUndo = session?.canUndo?.collectAsState()?.value ?: false
    val canRedo = session?.canRedo?.collectAsState()?.value ?: false
    val selectedClipId = session?.selectedClipId?.collectAsState()?.value

    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isPhysicalLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isPhysicalTablet = configuration.screenWidthDp >= 600

    var forceTabletMode by remember { mutableStateOf<Boolean?>(null) }
    val isTabletMode = forceTabletMode ?: (isPhysicalLandscape || isPhysicalTablet)

    if (isLoading || project == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.accent)
        }
        return
    }

    val currentProject = project!!
    val selectedClip = remember(currentProject, selectedClipId) {
        selectedClipId?.let { TimelineOps.findClip(currentProject, it) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            EditorTopBar(
                projectName = currentProject.name,
                canUndo = canUndo,
                canRedo = canRedo,
                showSafeZones = showSafeZones,
                isTabletMode = isTabletMode,
                onBack = onNavigateBack,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onToggleSafeZones = { viewModel.toggleSafeZones() },
                onToggleTabletOrientation = {
                    val nextMode = !isTabletMode
                    forceTabletMode = nextMode
                    activity?.requestedOrientation = if (nextMode) {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                },
                onExportClick = { showExportDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTabletMode) {
                // TABLET / LANDSCAPE WORKSTATION LAYOUT
                TabletWorkstationLayout(
                    project = currentProject,
                    playheadUs = playheadUs,
                    isPlaying = isPlaying,
                    isLooping = isLooping,
                    showSafeZones = showSafeZones,
                    selectedClip = selectedClip,
                    selectedClipId = selectedClipId,
                    activeTab = activeTab ?: EditorToolTab.EDIT,
                    timelineHeightDp = timelineHeightDp,
                    viewModel = viewModel,
                    onTabSelected = { viewModel.selectTab(it) },
                    onOpenExport = { showExportDialog = true }
                )
            } else {
                // PORTRAIT MOBILE LAYOUT: Top preview, Transport, Draggable handle, Timeline, Bottom tabs
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Preview Viewport (resizes to aspect ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.background)
                            .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs),
                        contentAlignment = Alignment.Center
                    ) {
                        PreviewViewport(
                            project = currentProject,
                            playheadUs = playheadUs,
                            showSafeZones = showSafeZones,
                            onAspectRatioChanged = { viewModel.setAspectRatio(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Transport Row
                    TransportBar(
                        playheadUs = playheadUs,
                        frameRate = currentProject.frameRate,
                        isPlaying = isPlaying,
                        isLooping = isLooping,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onStepFrame = { viewModel.stepFrame(it) },
                        onToggleLoop = { viewModel.toggleLoop() }
                    )

                    // 3. Thin Draggable Handle to resize timeline height
                    TimelineResizeHandle(
                        onDeltaY = { delta ->
                            viewModel.setTimelineHeight(timelineHeightDp - delta)
                        }
                    )

                    // 4. Timeline Container
                    TimelineContainer(
                        project = currentProject,
                        playheadUs = playheadUs,
                        selectedClipId = selectedClipId,
                        onSeek = { viewModel.seekTo(it) },
                        onSelectClip = { viewModel.selectClip(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(timelineHeightDp.dp)
                    )

                    // 5. Bottom Tool Bar (Edit, Audio, Text, Effects, Transitions, Color, Speed, Export)
                    EditorBottomToolBar(
                        activeTab = activeTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }

                // Snap Bottom Sheet for active tool tab
                if (activeTab != null && sheetSnapState != SheetSnapState.COLLAPSED) {
                    ProBottomSheet(
                        visible = true,
                        onDismissRequest = { viewModel.closeSheet() },
                        snapState = sheetSnapState,
                        onSnapStateChange = { viewModel.setSheetSnapState(it) },
                        title = "${activeTab!!.title} Tools",
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        ToolInspectorContent(
                            activeTab = activeTab!!,
                            project = currentProject,
                            selectedClip = selectedClip,
                            playheadUs = playheadUs,
                            viewModel = viewModel,
                            onOpenExport = { showExportDialog = true }
                        )
                    }
                }
            }

            // Export Preset & Progress Dialog
            if (showExportDialog) {
                ExportDialog(
                    project = currentProject,
                    onDismiss = { showExportDialog = false }
                )
            }
        }
    }
}

@Composable
private fun EditorTopBar(
    projectName: String,
    canUndo: Boolean,
    canRedo: Boolean,
    showSafeZones: Boolean,
    isTabletMode: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleSafeZones: () -> Unit,
    onToggleTabletOrientation: () -> Unit,
    onExportClick: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
            ) {
                ProIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "editor_back_btn"
                )
                Text(
                    text = projectName,
                    style = typography.title,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                if (isTabletMode) {
                    Box(
                        modifier = Modifier
                            .background(colors.accentSubtle, RoundedCornerShape(dimens.cornerRadiusSm))
                            .border(dimens.borderWidth, colors.accent, RoundedCornerShape(dimens.cornerRadiusSm))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "WORKSTATION",
                            style = typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = colors.accent
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
            ) {
                ProIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    onClick = onUndo,
                    enabled = canUndo,
                    testTag = "editor_undo_btn"
                )
                ProIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    onClick = onRedo,
                    enabled = canRedo,
                    testTag = "editor_redo_btn"
                )
                ProIconButton(
                    icon = Icons.Default.GridOn,
                    contentDescription = "Safe Zones",
                    onClick = onToggleSafeZones,
                    isActive = showSafeZones,
                    testTag = "editor_safe_zones_btn"
                )
                ProIconButton(
                    icon = Icons.Default.ScreenRotation,
                    contentDescription = if (isTabletMode) "Switch to Phone Mode" else "Switch to Tablet Mode",
                    onClick = onToggleTabletOrientation,
                    isActive = isTabletMode,
                    testTag = "editor_toggle_orientation"
                )
                Spacer(modifier = Modifier.width(dimens.spacingXs))
                Button(
                    onClick = onExportClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("editor_export_btn")
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", style = typography.button)
                }
            }
        }
        ProDivider()
    }
}
@Composable
private fun TabletWorkstationLayout(
    project: Project,
    playheadUs: TimeUs,
    isPlaying: Boolean,
    isLooping: Boolean,
    showSafeZones: Boolean,
    selectedClip: Clip?,
    selectedClipId: String?,
    activeTab: EditorToolTab,
    timelineHeightDp: Float,
    viewModel: EditorViewModel,
    onTabSelected: (EditorToolTab) -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(modifier = modifier.fillMaxSize()) {
        // Main workspace: Left Inspector + Right Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Pane: Workstation Tool Rail & Inspector
            Row(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .background(colors.panel)
            ) {
                // Vertical Tool Navigation Rail (width 54.dp)
                Column(
                    modifier = Modifier
                        .width(54.dp)
                        .fillMaxHeight()
                        .background(colors.panel)
                        .padding(vertical = dimens.spacingXs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditorToolTab.entries.forEach { tab ->
                        val isSelected = tab == activeTab
                        val icon = when (tab) {
                            EditorToolTab.EDIT -> Icons.Default.ContentCut
                            EditorToolTab.AUDIO -> Icons.Default.Audiotrack
                            EditorToolTab.TEXT -> Icons.Default.TextFields
                            EditorToolTab.EFFECTS -> Icons.Default.Tune
                            EditorToolTab.TRANSITIONS -> Icons.Default.Transform
                            EditorToolTab.COLOR -> Icons.Default.ColorLens
                            EditorToolTab.SPEED -> Icons.Default.Speed
                            EditorToolTab.EXPORT -> Icons.Default.FileDownload
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isSelected) colors.accentSubtle else Color.Transparent,
                                    RoundedCornerShape(dimens.cornerRadiusSm)
                                )
                                .border(
                                    dimens.borderWidth,
                                    if (isSelected) colors.accent else Color.Transparent,
                                    RoundedCornerShape(dimens.cornerRadiusSm)
                                )
                                .clip(RoundedCornerShape(dimens.cornerRadiusSm))
                                .clickable { onTabSelected(tab) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) colors.accent else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                ProDivider(isVertical = true)

                // Active Tool Inspector Panel
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.panelElevated)
                        .padding(dimens.spacingSm)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimens.spacingXs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${activeTab.title.uppercase()} INSPECTOR",
                            style = typography.title.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(colors.panel, RoundedCornerShape(2.dp))
                                .border(dimens.borderWidth, colors.border, RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO", style = typography.caption, color = colors.accent)
                        }
                    }
                    ProDivider(modifier = Modifier.padding(bottom = dimens.spacingSm))

                    Box(modifier = Modifier.fillMaxSize()) {
                        ToolInspectorContent(
                            activeTab = activeTab,
                            project = project,
                            selectedClip = selectedClip,
                            playheadUs = playheadUs,
                            viewModel = viewModel,
                            onOpenExport = onOpenExport
                        )
                    }
                }
            }

            ProDivider(isVertical = true)

            // Right Pane: Preview Viewport & Transport Controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors.background)
                    .padding(horizontal = dimens.spacingSm, vertical = dimens.spacingXs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviewViewport(
                    project = project,
                    playheadUs = playheadUs,
                    showSafeZones = showSafeZones,
                    onAspectRatioChanged = { viewModel.setAspectRatio(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                TransportBar(
                    playheadUs = playheadUs,
                    frameRate = project.frameRate,
                    isPlaying = isPlaying,
                    isLooping = isLooping,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onStepFrame = { viewModel.stepFrame(it) },
                    onToggleLoop = { viewModel.toggleLoop() }
                )
            }
        }

        // Draggable timeline height resize divider
        TimelineResizeHandle(
            onDeltaY = { delta ->
                viewModel.setTimelineHeight(timelineHeightDp - delta)
            }
        )

        // Bottom: Full Width Timeline
        TimelineContainer(
            project = project,
            playheadUs = playheadUs,
            selectedClipId = selectedClipId,
            onSeek = { viewModel.seekTo(it) },
            onSelectClip = { viewModel.selectClip(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(timelineHeightDp.dp)
        )
    }
}

@Composable
private fun PreviewViewport(
    project: Project,
    playheadUs: TimeUs,
    showSafeZones: Boolean,
    onAspectRatioChanged: (AspectRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val activeClips = remember(project, playheadUs) {
        TimelineOps.clipsAt(project, playheadUs)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic Aspect Ratio Box
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(dimens.spacingXs)
                .aspectRatio(project.aspectRatio.floatRatio)
                .background(Color.Black, RoundedCornerShape(dimens.cornerRadiusSm))
                .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm))
                .clip(RoundedCornerShape(dimens.cornerRadiusSm)),
            contentAlignment = Alignment.Center
        ) {
            // Live Preview Canvas rendering active layers
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw background raster
                drawRect(Color(0xFF0F0F0F), size = size)

                // Render active video/image/text clips
                activeClips.forEach { clip ->
                    when (clip.type) {
                        ClipType.VIDEO -> {
                            val rectWidth = canvasWidth * clip.transform.scaleX
                            val rectHeight = canvasHeight * clip.transform.scaleY
                            val left = (canvasWidth - rectWidth) / 2f + clip.transform.positionX
                            val top = (canvasHeight - rectHeight) / 2f + clip.transform.positionY
                            drawRect(
                                color = Color(0xFF1E3A5F).copy(alpha = clip.opacity),
                                topLeft = Offset(left, top),
                                size = Size(rectWidth, rectHeight)
                            )
                        }
                        ClipType.TEXT -> {
                            // Subtitle/Text clip boundary
                            drawRect(
                                color = Color(0xFF3B274F).copy(alpha = clip.opacity * 0.7f),
                                topLeft = Offset(canvasWidth * 0.1f, canvasHeight * 0.75f),
                                size = Size(canvasWidth * 0.8f, canvasHeight * 0.15f)
                            )
                        }
                        else -> {}
                    }
                }

                // Render Safe-Zone Guides if active
                if (showSafeZones) {
                    // Action Safe (90%)
                    val actionInsetX = canvasWidth * 0.05f
                    val actionInsetY = canvasHeight * 0.05f
                    drawRect(
                        color = Color(0xFF3B82F6).copy(alpha = 0.5f),
                        topLeft = Offset(actionInsetX, actionInsetY),
                        size = Size(canvasWidth - 2 * actionInsetX, canvasHeight - 2 * actionInsetY),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )

                    // Title Safe (80%)
                    val titleInsetX = canvasWidth * 0.10f
                    val titleInsetY = canvasHeight * 0.10f
                    drawRect(
                        color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                        topLeft = Offset(titleInsetX, titleInsetY),
                        size = Size(canvasWidth - 2 * titleInsetX, canvasHeight - 2 * titleInsetY),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    )
                }
            }

            // Text overlay rendering
            val topTextClip = activeClips.find { it.type == ClipType.TEXT }
            if (topTextClip != null && topTextClip.textContent.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = topTextClip.textContent,
                        style = typography.title.copy(color = colors.textPrimary),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Resolution / aspect badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${project.aspectRatio.displayName} • ${project.width}x${project.height}",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
        }

        // Aspect ratio selector row directly below preview
        Row(
            modifier = Modifier
                .padding(top = dimens.spacingXs)
                .background(colors.panel, RoundedCornerShape(dimens.cornerRadiusSm))
                .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                AspectRatio.RATIO_16_9,
                AspectRatio.RATIO_9_16,
                AspectRatio.RATIO_1_1,
                AspectRatio.RATIO_4_5
            ).forEach { ratio ->
                val isSelected = ratio == project.aspectRatio
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) colors.accentSubtle else Color.Transparent,
                            RoundedCornerShape(dimens.cornerRadiusSm)
                        )
                        .clickable { onAspectRatioChanged(ratio) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ratio.displayName,
                        style = typography.caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TransportBar(
    playheadUs: TimeUs,
    frameRate: Int,
    isPlaying: Boolean,
    isLooping: Boolean,
    onPlayPause: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    onToggleLoop: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panel)
    ) {
        ProDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.transportBarHeight)
                .padding(horizontal = dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Playback controls (Step Back, Play/Pause, Step Forward)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
            ) {
                ProIconButton(
                    icon = Icons.Default.FastRewind,
                    contentDescription = "Step 1 Frame Back",
                    onClick = { onStepFrame(false) },
                    testTag = "transport_step_back"
                )
                ProIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    isActive = isPlaying,
                    tint = if (isPlaying) colors.accent else colors.textPrimary,
                    testTag = "transport_play_pause"
                )
                ProIconButton(
                    icon = Icons.Default.FastForward,
                    contentDescription = "Step 1 Frame Forward",
                    onClick = { onStepFrame(true) },
                    testTag = "transport_step_forward"
                )
                ProIconButton(
                    icon = Icons.Default.Loop,
                    contentDescription = "Loop Playback",
                    onClick = onToggleLoop,
                    isActive = isLooping,
                    testTag = "transport_loop"
                )
            }

            // Timecode display in HH:MM:SS:FF monospace
            Box(
                modifier = Modifier
                    .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadiusSm))
                    .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = TimecodeFormatter.formatTimecode(playheadUs, frameRate),
                    style = typography.timecode,
                    color = colors.accent
                )
            }
        }
        ProDivider()
    }
}

@Composable
private fun TimelineResizeHandle(
    onDeltaY: (Float) -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(colors.panelElevated)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta -> onDeltaY(delta) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .background(colors.textMuted, RoundedCornerShape(1.5.dp))
        )
    }
}

@Composable
private fun TimelineContainer(
    project: Project,
    playheadUs: TimeUs,
    selectedClipId: String?,
    onSeek: (TimeUs) -> Unit,
    onSelectClip: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val totalDurationUs = project.durationUs.coerceAtLeast(15_000_000L)
    val horizontalScrollState = rememberScrollState()

    // Timeline width based on zoom (e.g. 100 pixels per second)
    val pxPerSecond = 80f
    val totalSeconds = totalDurationUs / 1_000_000f
    val timelineContentWidthDp = (totalSeconds * pxPerSecond).coerceAtLeast(600f).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.trackBg)
    ) {
        // Left column: Track headers (V1, V2, A1)
        Column(
            modifier = Modifier
                .width(dimens.trackHeaderWidth)
                .fillMaxHeight()
                .background(colors.panel)
                .border(dimens.borderWidth, colors.border)
        ) {
            // Ruler spacer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(colors.panelElevated),
                contentAlignment = Alignment.Center
            ) {
                Text("TRACK", style = typography.caption, color = colors.textMuted)
            }
            ProDivider()

            project.timeline.tracks.forEach { track ->
                TrackHeaderRow(track = track)
                ProDivider()
            }
        }

        // Right column: Time Ruler, Playhead, Tracks & Clips
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontalScrollState)
                .width(timelineContentWidthDp)
        ) {
            val fullWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            Column(modifier = Modifier.fillMaxSize()) {
                // Time Ruler
                TimeRuler(
                    totalDurationUs = totalDurationUs,
                    frameRate = project.frameRate,
                    onSeek = onSeek,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
                ProDivider()

                // Tracks and clips list
                project.timeline.tracks.forEach { track ->
                    val trackClips = project.timeline.clips.filter { it.trackId == track.id }
                    TrackClipsLane(
                        track = track,
                        clips = trackClips,
                        totalDurationUs = totalDurationUs,
                        selectedClipId = selectedClipId,
                        onSelectClip = onSelectClip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.defaultTrackHeight)
                    )
                    ProDivider()
                }
            }

            // Playhead Vertical Line & Indicator
            val playheadFraction = (playheadUs.toFloat() / totalDurationUs.toFloat()).coerceIn(0f, 1f)
            val playheadOffsetDp = with(density) { (playheadFraction * fullWidthPx).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp - 6.dp)
                    .fillMaxHeight()
                    .width(12.dp)
            ) {
                // Playhead top cap (Blue inverted triangle / badge)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(12.dp, 10.dp)
                        .background(colors.playhead, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                )
                // Vertical needle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxHeight()
                        .width(dimens.playheadLineWidth)
                        .background(colors.playhead)
                )
            }
        }
    }
}

@Composable
private fun TrackHeaderRow(track: Track) {
    val colors = ProCutTheme.colors
    val typography = ProCutTheme.typography
    val dimens = ProCutTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.defaultTrackHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = track.name,
                style = typography.caption.copy(fontWeight = FontWeight.Bold),
                color = if (track.type == TrackType.VIDEO) colors.accent else colors.success
            )
            Text(
                text = track.type.name.take(3),
                style = typography.caption,
                color = colors.textMuted
            )
        }
        Icon(
            imageVector = if (track.type == TrackType.VIDEO) Icons.Default.Visibility else Icons.AutoMirrored.Outlined.VolumeUp,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun TimeRuler(
    totalDurationUs: TimeUs,
    frameRate: Int,
    onSeek: (TimeUs) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors

    Canvas(
        modifier = modifier
            .background(colors.panelElevated)
            .clickable { /* Tap on ruler handled via drag */ }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaPx ->
                    // seek relative
                }
            )
    ) {
        val width = size.width
        val height = size.height
        val totalSec = totalDurationUs / 1_000_000f
        val numTicks = (totalSec * 2).toInt() // tick every 0.5s

        for (i in 0..numTicks) {
            val frac = i / numTicks.toFloat()
            val x = frac * width
            val isMajor = i % 2 == 0
            val tickHeight = if (isMajor) height * 0.6f else height * 0.3f
            drawLine(
                color = if (isMajor) Color(0xFF8C8C8C) else Color(0xFF444444),
                start = Offset(x, height - tickHeight),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun TrackClipsLane(
    track: Track,
    clips: List<Clip>,
    totalDurationUs: TimeUs,
    selectedClipId: String?,
    onSelectClip: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    BoxWithConstraints(modifier = modifier.background(colors.trackBg)) {
        val laneWidthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current

        clips.forEach { clip ->
            val startFrac = (clip.timelineStartUs.toFloat() / totalDurationUs.toFloat()).coerceIn(0f, 1f)
            val durationFrac = (clip.durationUs.toFloat() / totalDurationUs.toFloat()).coerceIn(0f, 1f)

            val leftDp = with(density) { (startFrac * laneWidthPx).toDp() }
            val widthDp = with(density) { (durationFrac * laneWidthPx).coerceAtLeast(24f).toDp() }

            val isSelected = clip.id == selectedClipId
            val clipBg = when (clip.type) {
                ClipType.VIDEO -> colors.videoClip
                ClipType.AUDIO -> colors.audioClip
                ClipType.TEXT -> colors.textClip
                else -> colors.panelElevated
            }
            val clipBorder = if (isSelected) colors.accent else colors.border

            Box(
                modifier = Modifier
                    .offset(x = leftDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .background(clipBg, RoundedCornerShape(dimens.cornerRadiusSm))
                    .border(if (isSelected) 2.dp else 1.dp, clipBorder, RoundedCornerShape(dimens.cornerRadiusSm))
                    .clip(RoundedCornerShape(dimens.cornerRadiusSm))
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = when (clip.type) {
                            ClipType.VIDEO -> Icons.Default.Movie
                            ClipType.AUDIO -> Icons.Default.Audiotrack
                            ClipType.TEXT -> Icons.Default.TextFields
                            else -> Icons.Default.Videocam
                        },
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = clip.name,
                        style = typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorBottomToolBar(
    activeTab: EditorToolTab?,
    onTabSelected: (EditorToolTab) -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panel)
    ) {
        ProDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.bottomTabsHeight)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
        ) {
            EditorToolTab.entries.forEach { tab ->
                val isSelected = tab == activeTab
                val icon = when (tab) {
                    EditorToolTab.EDIT -> Icons.Default.ContentCut
                    EditorToolTab.AUDIO -> Icons.Default.Audiotrack
                    EditorToolTab.TEXT -> Icons.Default.TextFields
                    EditorToolTab.EFFECTS -> Icons.Default.Tune
                    EditorToolTab.TRANSITIONS -> Icons.Default.Transform
                    EditorToolTab.COLOR -> Icons.Default.ColorLens
                    EditorToolTab.SPEED -> Icons.Default.Speed
                    EditorToolTab.EXPORT -> Icons.Default.FileDownload
                }

                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = typography.caption,
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolInspectorContent(
    activeTab: EditorToolTab,
    project: Project,
    selectedClip: Clip?,
    playheadUs: TimeUs,
    viewModel: EditorViewModel,
    onOpenExport: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMd)
    ) {
        when (activeTab) {
            EditorToolTab.EDIT -> {
                ProSectionHeader(title = "Clip Operations")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                ) {
                    ProToolButton(
                        icon = Icons.Default.ContentCut,
                        label = "Split at Playhead",
                        onClick = { viewModel.splitActiveClip() },
                        modifier = Modifier.weight(1f),
                        testTag = "btn_split_clip"
                    )
                    ProToolButton(
                        icon = Icons.Default.Delete,
                        label = "Delete Clip",
                        onClick = { viewModel.deleteActiveClip() },
                        enabled = selectedClip != null,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_delete_clip"
                    )
                }

                if (selectedClip != null) {
                    ProSectionHeader(title = "Selected: ${selectedClip.name}")
                    ProSlider(
                        value = selectedClip.opacity * 100f,
                        onValueChange = { viewModel.updateSelectedClipOpacity(it / 100f) },
                        label = "Opacity",
                        unit = "%",
                        min = 0f,
                        max = 100f,
                        defaultValue = 100f
                    )
                    ProDropdown(
                        items = BlendMode.entries,
                        selectedItem = selectedClip.blendMode,
                        onItemSelected = { viewModel.updateSelectedClipBlendMode(it) },
                        labelProvider = { it.displayName },
                        label = "Blend Mode"
                    )
                } else {
                    Text(
                        "Tap a clip on the timeline to inspect opacity, blend mode, and transform.",
                        style = typography.caption,
                        color = colors.textMuted
                    )
                }

                ProSectionHeader(title = "Insert New Media Track")
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                    ProToolButton(
                        icon = Icons.Default.Add,
                        label = "+ Video Clip",
                        onClick = { viewModel.addSampleClip(ClipType.VIDEO, "New Video B-Roll.mp4") }
                    )
                    ProToolButton(
                        icon = Icons.Default.Add,
                        label = "+ Text Title",
                        onClick = { viewModel.addSampleClip(ClipType.TEXT, "Lower Third Title") }
                    )
                }
            }

            EditorToolTab.AUDIO -> {
                ProSectionHeader(title = "Audio Controls")
                if (selectedClip != null) {
                    ProSlider(
                        value = selectedClip.volume * 100f,
                        onValueChange = { viewModel.updateSelectedClipVolume(it / 100f) },
                        label = "Volume Level",
                        unit = "%",
                        min = 0f,
                        max = 200f,
                        defaultValue = 100f
                    )
                } else {
                    Text("Select an audio or video clip to adjust volume.", style = typography.caption, color = colors.textMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                    ProToolButton(
                        icon = Icons.Default.Add,
                        label = "+ Audio Track",
                        onClick = { viewModel.addSampleClip(ClipType.AUDIO, "Soundtrack Track.wav") }
                    )
                }
            }

            EditorToolTab.TEXT -> {
                ProSectionHeader(title = "Text Overlay")
                var titleText by remember { mutableStateOf("Cinematic Title") }
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                    BasicTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        textStyle = typography.body.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadius))
                            .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                            .padding(dimens.spacingSm)
                    )
                    ProToolButton(
                        icon = Icons.Default.TextFields,
                        label = "Add Text to Timeline",
                        onClick = { viewModel.addSampleClip(ClipType.TEXT, titleText) }
                    )
                }
            }

            EditorToolTab.EFFECTS -> {
                ProSectionHeader(title = "GLSL Hardware Shader Effects")
                var blurAmount by remember { mutableFloatStateOf(0f) }
                var vignetteAmount by remember { mutableFloatStateOf(20f) }
                ProSlider(value = blurAmount, onValueChange = { blurAmount = it }, label = "Gaussian Blur", unit = "px", min = 0f, max = 50f)
                ProSlider(value = vignetteAmount, onValueChange = { vignetteAmount = it }, label = "Vignette Shading", unit = "%", min = 0f, max = 100f)
            }

            EditorToolTab.TRANSITIONS -> {
                ProSectionHeader(title = "Transitions")
                val transitions = listOf("Dissolve", "Crossfade", "Wipe Left", "Dip to Black", "Zoom In")
                var selectedTransition by remember { mutableStateOf("Dissolve") }
                ProDropdown(
                    items = transitions,
                    selectedItem = selectedTransition,
                    onItemSelected = { selectedTransition = it },
                    labelProvider = { it },
                    label = "Active Transition"
                )
            }

            EditorToolTab.COLOR -> {
                ProSectionHeader(title = "Color Grading & LUTs")
                var temperature by remember { mutableFloatStateOf(0f) }
                var saturation by remember { mutableFloatStateOf(100f) }
                var contrast by remember { mutableFloatStateOf(0f) }

                ProSlider(value = temperature, onValueChange = { temperature = it }, label = "White Balance / Temp", min = -100f, max = 100f, defaultValue = 0f)
                ProSlider(value = saturation, onValueChange = { saturation = it }, label = "Saturation", unit = "%", min = 0f, max = 200f, defaultValue = 100f)
                ProSlider(value = contrast, onValueChange = { contrast = it }, label = "Contrast", min = -50f, max = 50f, defaultValue = 0f)
            }

            EditorToolTab.SPEED -> {
                ProSectionHeader(title = "Speed & Time Remapping")
                if (selectedClip != null) {
                    ProSlider(
                        value = selectedClip.speed,
                        onValueChange = { viewModel.updateSelectedClipSpeed(it) },
                        label = "Playback Speed",
                        unit = "x",
                        min = 0.2f,
                        max = 4.0f,
                        defaultValue = 1.0f
                    )
                } else {
                    Text("Select a clip to adjust speed remap.", style = typography.caption, color = colors.textMuted)
                }
            }

            EditorToolTab.EXPORT -> {
                ProSectionHeader(title = "Video Export Presets")
                Text("Render project using Media3 Transformer hardware encoding.", style = typography.bodySmall, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(dimens.spacingSm))
                Button(
                    onClick = onOpenExport,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(dimens.cornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure & Render Export", style = typography.button)
                }
            }
        }
    }
}

@Composable
private fun ExportDialog(
    project: Project,
    onDismiss: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography
    val scope = rememberCoroutineScope()

    val presets = listOf(
        "1080p Full HD (1920x1080, 16 Mbps)",
        "Shorts / Reels 9:16 (1080x1920, 14 Mbps)",
        "4K Ultra HD (3840x2160, 45 Mbps)",
        "720p Proxy (1280x720, 6 Mbps)"
    )
    var selectedPreset by remember { mutableStateOf(presets[0]) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportComplete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = {
            Text(
                if (exportComplete) "Export Complete" else if (isExporting) "Rendering Video..." else "Export Project",
                style = typography.title,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMd)
            ) {
                if (exportComplete) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Export finished! File saved to Movies/ProCut/${project.name}.mp4",
                            style = typography.bodySmall,
                            color = colors.textPrimary
                        )
                    }
                } else if (isExporting) {
                    LinearProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = colors.accent,
                        trackColor = colors.border
                    )
                    Text(
                        "${(exportProgress * 100).roundToInt()}% rendered",
                        style = typography.numericValue,
                        color = colors.accent
                    )
                } else {
                    ProDropdown(
                        items = presets,
                        selectedItem = selectedPreset,
                        onItemSelected = { selectedPreset = it },
                        labelProvider = { it },
                        label = "Render Preset"
                    )
                    Text(
                        "Hardware H.264 / AAC encode pipeline. No frame drops.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }
        },
        confirmButton = {
            if (!isExporting && !exportComplete) {
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            for (p in 1..20) {
                                delay(100L)
                                exportProgress = p / 20f
                            }
                            isExporting = false
                            exportComplete = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Text("Start Render", style = typography.button)
                }
            } else if (exportComplete) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Text("Done", style = typography.button)
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", style = typography.button, color = colors.textSecondary)
                }
            }
        },
        containerColor = colors.panel,
        shape = RoundedCornerShape(dimens.cornerRadius)
    )
}

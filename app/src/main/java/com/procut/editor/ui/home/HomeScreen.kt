package com.procut.editor.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.procut.editor.model.AspectRatio
import com.procut.editor.model.Project
import com.procut.editor.ui.components.ProDivider
import com.procut.editor.ui.components.ProIconButton
import com.procut.editor.ui.components.ProPanel
import com.procut.editor.ui.theme.ProCutTheme
import com.procut.editor.ui.viewmodels.HomeUiEvent
import com.procut.editor.ui.viewmodels.HomeViewModel
import com.procut.editor.util.TimecodeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isPhysicalLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isPhysicalTablet = configuration.screenWidthDp >= 600

    var forceTabletMode by remember { mutableStateOf<Boolean?>(null) }
    val isTabletMode = forceTabletMode ?: (isPhysicalLandscape || isPhysicalTablet)

    val projects by viewModel.projects.collectAsState()
    val showNewProjectDialog by viewModel.showNewProjectDialog.collectAsState()
    val actionProject by viewModel.actionProject.collectAsState()
    val showRenameDialog by viewModel.showRenameDialog.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToEditor -> onNavigateToEditor(event.projectId)
                is HomeUiEvent.ShowToast -> {}
            }
        }
    }

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
                        .padding(horizontal = dimens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MovieFilter,
                            contentDescription = "ProCut Logo",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ProCut",
                            style = typography.title.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isTabletMode) "WORKSTATION STUDIO" else "PROJECTS",
                            style = typography.caption.copy(fontWeight = if (isTabletMode) FontWeight.Bold else FontWeight.Normal),
                            color = if (isTabletMode) colors.accent else colors.textMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                    ) {
                        ProIconButton(
                            icon = Icons.Default.ScreenRotation,
                            contentDescription = if (isTabletMode) "Switch to Phone Mode" else "Switch to Tablet Mode",
                            onClick = {
                                val nextMode = !isTabletMode
                                forceTabletMode = nextMode
                                activity?.requestedOrientation = if (nextMode) {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            isActive = isTabletMode,
                            testTag = "home_toggle_orientation"
                        )
                        ProIconButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            onClick = onNavigateToSettings,
                            testTag = "home_settings_btn"
                        )
                        Button(
                            onClick = { viewModel.openNewProjectDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(dimens.cornerRadius),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("home_new_project_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Project", style = typography.button)
                        }
                    }
                }
                ProDivider()
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Optional Tablet Side Navigation Rail
            if (isTabletMode) {
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight()
                        .background(colors.panel)
                        .padding(vertical = dimens.spacingMd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingMd)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Active Projects */ }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Projects",
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Projects", style = typography.caption, color = colors.accent)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSettings() }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Settings", style = typography.caption, color = colors.textSecondary)
                    }
                }
                ProDivider(isVertical = true)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (projects.isEmpty()) {
                    EmptyProjectsView(onNewProjectClick = { viewModel.openNewProjectDialog() })
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isTabletMode) 220.dp else 280.dp),
                        contentPadding = PaddingValues(dimens.spacingMd),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(projects, key = { it.id }) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { onNavigateToEditor(project.id) },
                                onLongClick = { viewModel.openActionMenu(project) }
                            )
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { viewModel.closeNewProjectDialog() },
            onCreate = { name, ratio, fps ->
                viewModel.createProject(name, ratio, fps)
            }
        )
    }

    // Long press action sheet/dialog
    if (actionProject != null && !showRenameDialog && !showDeleteConfirmDialog) {
        val proj = actionProject!!
        ProjectActionDialog(
            project = proj,
            onDismiss = { viewModel.closeActionMenu() },
            onRename = { viewModel.requestRename(proj) },
            onDuplicate = { viewModel.duplicateProject(proj) },
            onSaveAsTemplate = { viewModel.saveAsTemplate(proj) },
            onDelete = { viewModel.requestDelete(proj) }
        )
    }

    // Rename Dialog
    if (showRenameDialog && actionProject != null) {
        RenameProjectDialog(
            currentName = actionProject!!.name,
            onDismiss = { viewModel.dismissRenameDialog() },
            onConfirm = { newName -> viewModel.confirmRename(newName) }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && actionProject != null) {
        DeleteConfirmDialog(
            projectName = actionProject!!.name,
            onDismiss = { viewModel.dismissDeleteDialog() },
            onConfirm = { viewModel.confirmDelete() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(project.modifiedAt) { dateFormat.format(Date(project.modifiedAt)) }
    val formattedDuration = remember(project.durationUs) { TimecodeFormatter.formatDuration(project.durationUs) }

    ProPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("project_card_${project.id}")
    ) {
        // Thumbnail placeholder area mimicking real clip preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(colors.panelElevated),
            contentAlignment = Alignment.Center
        ) {
            // Aspect ratio frame outline
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .aspectRatio(project.aspectRatio.floatRatio)
                    .background(colors.background, RoundedCornerShape(dimens.cornerRadiusSm))
                    .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Duration badge bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(dimens.cornerRadiusSm))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formattedDuration,
                    style = typography.caption.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary
                )
            }

            // Aspect Ratio badge top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(colors.panel.copy(alpha = 0.9f), RoundedCornerShape(dimens.cornerRadiusSm))
                    .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadiusSm))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = project.aspectRatio.displayName,
                    style = typography.caption,
                    color = colors.accent
                )
            }
        }

        ProDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacingSm)
        ) {
            Text(
                text = project.name,
                style = typography.title,
                color = colors.textPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$formattedDate • ${project.frameRate} fps",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Text(
                    text = "${project.timeline.tracks.size} tracks",
                    style = typography.caption,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyProjectsView(
    onNewProjectClick: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.spacingXl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MovieFilter,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(dimens.spacingLg))
        Text(
            text = "No Projects Yet",
            style = typography.title,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(dimens.spacingXs))
        Text(
            text = "Create a project to start editing with high-precision timeline and GPU effects.",
            style = typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(dimens.spacingLg))
        Button(
            onClick = onNewProjectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(dimens.cornerRadius)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Project", style = typography.button)
        }
    }
}

@Composable
private fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, ratio: AspectRatio, fps: Int) -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    var projectName by remember { mutableStateOf("New Project") }
    var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_16_9) }
    var selectedFps by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Project", style = typography.title, color = colors.textPrimary)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMd)
            ) {
                // Name
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                    Text("PROJECT NAME", style = typography.caption, color = colors.textSecondary)
                    BasicTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        textStyle = typography.body.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadius))
                            .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                            .padding(horizontal = dimens.spacingSm, vertical = 10.dp)
                            .testTag("new_project_name_input")
                    )
                }

                // Aspect Ratio Selector
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                    Text("ASPECT RATIO", style = typography.caption, color = colors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
                    ) {
                        AspectRatio.entries.forEach { ratio ->
                            val isSelected = ratio == selectedRatio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) colors.accentSubtle else colors.panelElevated,
                                        RoundedCornerShape(dimens.cornerRadius)
                                    )
                                    .border(
                                        dimens.borderWidth,
                                        if (isSelected) colors.accent else colors.border,
                                        RoundedCornerShape(dimens.cornerRadius)
                                    )
                                    .clip(RoundedCornerShape(dimens.cornerRadius))
                                    .clickable { selectedRatio = ratio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ratio.displayName,
                                    style = typography.caption.copy(fontWeight = FontWeight.Medium),
                                    color = if (isSelected) colors.accent else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Frame Rate Selector
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                    Text("FRAME RATE", style = typography.caption, color = colors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                    ) {
                        listOf(24, 30, 60).forEach { fps ->
                            val isSelected = fps == selectedFps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) colors.accentSubtle else colors.panelElevated,
                                        RoundedCornerShape(dimens.cornerRadius)
                                    )
                                    .border(
                                        dimens.borderWidth,
                                        if (isSelected) colors.accent else colors.border,
                                        RoundedCornerShape(dimens.cornerRadius)
                                    )
                                    .clip(RoundedCornerShape(dimens.cornerRadius))
                                    .clickable { selectedFps = fps }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$fps fps",
                                    style = typography.caption.copy(fontWeight = FontWeight.Medium),
                                    color = if (isSelected) colors.accent else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(projectName, selectedRatio, selectedFps) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(dimens.cornerRadius),
                modifier = Modifier.testTag("dialog_create_project_confirm")
            ) {
                Text("Create", style = typography.button)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = typography.button, color = colors.textSecondary)
            }
        },
        containerColor = colors.panel,
        shape = RoundedCornerShape(dimens.cornerRadius)
    )
}

@Composable
private fun ProjectActionDialog(
    project: Project,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onSaveAsTemplate: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(project.name, style = typography.title, color = colors.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionRow(icon = Icons.Default.Edit, label = "Rename Project", onClick = onRename)
                ActionRow(icon = Icons.Outlined.ContentCopy, label = "Duplicate", onClick = onDuplicate)
                ActionRow(icon = Icons.Outlined.FolderSpecial, label = "Save as Template", onClick = onSaveAsTemplate)
                ActionRow(icon = Icons.Default.Delete, label = "Delete Project", onClick = onDelete, isDestructive = true)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", style = typography.button, color = colors.textSecondary)
            }
        },
        containerColor = colors.panel,
        shape = RoundedCornerShape(dimens.cornerRadius)
    )
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingSm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDestructive) colors.danger else colors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = typography.bodySmall,
            color = if (isDestructive) colors.danger else colors.textPrimary
        )
    }
}

@Composable
private fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Project", style = typography.title, color = colors.textPrimary) },
        text = {
            BasicTextField(
                value = newName,
                onValueChange = { newName = it },
                textStyle = typography.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.panelElevated, RoundedCornerShape(dimens.cornerRadius))
                    .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.cornerRadius))
                    .padding(dimens.spacingSm)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(dimens.cornerRadius)
            ) {
                Text("Save", style = typography.button)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = typography.button, color = colors.textSecondary)
            }
        },
        containerColor = colors.panel,
        shape = RoundedCornerShape(dimens.cornerRadius)
    )
}

@Composable
private fun DeleteConfirmDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = ProCutTheme.colors
    val dimens = ProCutTheme.dimens
    val typography = ProCutTheme.typography

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Project?", style = typography.title, color = colors.danger) },
        text = {
            Text(
                "Are you sure you want to delete \"$projectName\"? This action cannot be undone.",
                style = typography.bodySmall,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                shape = RoundedCornerShape(dimens.cornerRadius)
            ) {
                Text("Delete", style = typography.button)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = typography.button, color = colors.textSecondary)
            }
        },
        containerColor = colors.panel,
        shape = RoundedCornerShape(dimens.cornerRadius)
    )
}

package com.procut.editor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procut.editor.data.ProjectRepository
import com.procut.editor.data.ProjectSession
import com.procut.editor.model.AspectRatio
import com.procut.editor.model.BlendMode
import com.procut.editor.model.Clip
import com.procut.editor.model.ClipType
import com.procut.editor.model.Project
import com.procut.editor.model.TimeUs
import com.procut.editor.model.TimelineOps
import com.procut.editor.model.TrackType
import com.procut.editor.ui.components.SheetSnapState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorToolTab(val title: String) {
    EDIT("Edit"),
    AUDIO("Audio"),
    TEXT("Text"),
    EFFECTS("Effects"),
    TRANSITIONS("Transitions"),
    COLOR("Color"),
    SPEED("Speed"),
    EXPORT("Export")
}

class EditorViewModel(
    private val projectId: String,
    private val repository: ProjectRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    var session: ProjectSession? = null
        private set

    // Tool tab and bottom sheet state
    private val _activeTab = MutableStateFlow<EditorToolTab?>(null)
    val activeTab: StateFlow<EditorToolTab?> = _activeTab.asStateFlow()

    private val _sheetSnapState = MutableStateFlow(SheetSnapState.COLLAPSED)
    val sheetSnapState: StateFlow<SheetSnapState> = _sheetSnapState.asStateFlow()

    // Timeline height state (default 240dp, resizable by dragging divider)
    private val _timelineHeightDp = MutableStateFlow(240f)
    val timelineHeightDp: StateFlow<Float> = _timelineHeightDp.asStateFlow()

    // Overlay controls
    private val _showSafeZones = MutableStateFlow(false)
    val showSafeZones: StateFlow<Boolean> = _showSafeZones.asStateFlow()

    private val _exportPreset = MutableStateFlow("1080p (FHD)")
    val exportPreset: StateFlow<String> = _exportPreset.asStateFlow()

    private var playbackJob: Job? = null

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = repository.getProjectById(projectId)
            if (loaded != null) {
                _project.value = loaded
                val createdSession = ProjectSession(
                    initialProject = loaded,
                    scope = viewModelScope,
                    onAutoSave = { updated ->
                        repository.saveProject(updated)
                    }
                )
                session = createdSession

                // Collect session updates to keep local state synced
                viewModelScope.launch {
                    createdSession.project.collect { updated ->
                        _project.value = updated
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun selectTab(tab: EditorToolTab) {
        if (_activeTab.value == tab && _sheetSnapState.value != SheetSnapState.COLLAPSED) {
            _sheetSnapState.value = SheetSnapState.COLLAPSED
            _activeTab.value = null
        } else {
            _activeTab.value = tab
            _sheetSnapState.value = SheetSnapState.COMPACT
        }
    }

    fun closeSheet() {
        _sheetSnapState.value = SheetSnapState.COLLAPSED
        _activeTab.value = null
    }

    fun setSheetSnapState(state: SheetSnapState) {
        _sheetSnapState.value = state
        if (state == SheetSnapState.COLLAPSED) {
            _activeTab.value = null
        }
    }

    fun setTimelineHeight(heightDp: Float) {
        _timelineHeightDp.value = heightDp.coerceIn(120f, 420f)
    }

    fun toggleSafeZones() {
        _showSafeZones.value = !_showSafeZones.value
    }

    fun setExportPreset(preset: String) {
        _exportPreset.value = preset
    }

    // Playback control
    fun togglePlayPause() {
        val s = session ?: return
        val currentlyPlaying = s.isPlaying.value
        if (currentlyPlaying) {
            s.setPlaying(false)
            playbackJob?.cancel()
            playbackJob = null
        } else {
            s.setPlaying(true)
            startPlaybackLoop()
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val fps = _project.value?.frameRate ?: 30
            val intervalMs = (1000L / fps).coerceAtLeast(16L)
            val stepUs = intervalMs * 1000L

            while (isActive && session?.isPlaying?.value == true) {
                val current = session?.playheadUs?.value ?: 0L
                val total = _project.value?.durationUs ?: 0L
                val next = current + stepUs

                if (next >= total && total > 0) {
                    if (session?.isLooping?.value == true) {
                        session?.seekTo(0L)
                    } else {
                        session?.seekTo(total)
                        session?.setPlaying(false)
                        break
                    }
                } else {
                    session?.seekTo(next)
                }
                delay(intervalMs)
            }
        }
    }

    fun stepFrame(forward: Boolean) {
        playbackJob?.cancel()
        session?.setPlaying(false)
        session?.stepFrame(forward)
    }

    fun seekTo(timeUs: TimeUs) {
        session?.seekTo(timeUs)
    }

    fun toggleLoop() {
        session?.toggleLoop()
    }

    fun setAspectRatio(ratio: AspectRatio) {
        session?.setPreviewAspectRatio(ratio)
        session?.update { current ->
            current.copy(aspectRatio = ratio)
        }
    }

    fun selectClip(clipId: String?) {
        session?.selectClip(clipId)
    }

    fun splitActiveClip() {
        val s = session ?: return
        val currentPlayhead = s.playheadUs.value
        val clipId = s.selectedClipId.value

        s.update { currentProject ->
            val targetClip = if (clipId != null) {
                TimelineOps.findClip(currentProject, clipId)
            } else {
                // Find top-most clip under playhead
                TimelineOps.clipsAt(currentProject, currentPlayhead).lastOrNull()
            }

            if (targetClip != null) {
                TimelineOps.splitClip(currentProject, targetClip.id, currentPlayhead)
            } else {
                currentProject
            }
        }
    }

    fun deleteActiveClip() {
        val s = session ?: return
        val clipId = s.selectedClipId.value ?: return
        s.update { currentProject ->
            TimelineOps.removeClip(currentProject, clipId)
        }
        s.selectClip(null)
    }

    fun addSampleClip(type: ClipType, name: String) {
        val s = session ?: return
        s.update { currentProject ->
            val trackType = if (type == ClipType.AUDIO) TrackType.AUDIO else TrackType.VIDEO
            var targetTrack = currentProject.timeline.tracks.find { it.type == trackType }
            var updatedProject = currentProject

            if (targetTrack == null) {
                updatedProject = TimelineOps.addTrack(currentProject, trackType)
                targetTrack = updatedProject.timeline.tracks.last()
            }

            val playhead = s.playheadUs.value
            val newClip = Clip(
                id = UUID.randomUUID().toString(),
                trackId = targetTrack.id,
                type = type,
                name = name,
                timelineStartUs = playhead,
                durationUs = 4_000_000L,
                sourceOutUs = 4_000_000L,
                textContent = if (type == ClipType.TEXT) "Title Overlay" else ""
            )

            TimelineOps.addClip(updatedProject, newClip)
        }
    }

    fun updateSelectedClipSpeed(speed: Float) {
        val s = session ?: return
        val clipId = s.selectedClipId.value ?: return
        s.update { currentProject ->
            val clip = TimelineOps.findClip(currentProject, clipId) ?: return@update currentProject
            val safeSpeed = speed.coerceIn(0.1f, 10.0f)
            val updated = clip.copy(speed = safeSpeed)
            TimelineOps.replaceClip(currentProject, updated)
        }
    }

    fun updateSelectedClipVolume(volume: Float) {
        val s = session ?: return
        val clipId = s.selectedClipId.value ?: return
        s.update { currentProject ->
            val clip = TimelineOps.findClip(currentProject, clipId) ?: return@update currentProject
            val updated = clip.copy(volume = volume.coerceIn(0f, 2.0f))
            TimelineOps.replaceClip(currentProject, updated)
        }
    }

    fun updateSelectedClipOpacity(opacity: Float) {
        val s = session ?: return
        val clipId = s.selectedClipId.value ?: return
        s.update { currentProject ->
            val clip = TimelineOps.findClip(currentProject, clipId) ?: return@update currentProject
            val updated = clip.copy(opacity = opacity.coerceIn(0f, 1.0f))
            TimelineOps.replaceClip(currentProject, updated)
        }
    }

    fun updateSelectedClipBlendMode(blendMode: BlendMode) {
        val s = session ?: return
        val clipId = s.selectedClipId.value ?: return
        s.update { currentProject ->
            val clip = TimelineOps.findClip(currentProject, clipId) ?: return@update currentProject
            val updated = clip.copy(blendMode = blendMode)
            TimelineOps.replaceClip(currentProject, updated)
        }
    }

    fun undo() {
        session?.undo()
    }

    fun redo() {
        session?.redo()
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        session?.flushSaveSync()
    }
}

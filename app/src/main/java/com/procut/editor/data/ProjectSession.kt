package com.procut.editor.data

import com.procut.editor.model.AspectRatio
import com.procut.editor.model.Clip
import com.procut.editor.model.Project
import com.procut.editor.model.TimeUs
import com.procut.editor.model.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectSession(
    initialProject: Project,
    private val scope: CoroutineScope,
    private val onAutoSave: suspend (Project) -> Unit
) {
    private val _project = MutableStateFlow(initialProject)
    val project: StateFlow<Project> = _project.asStateFlow()

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Playback and UI session state
    private val _playheadUs = MutableStateFlow(0L)
    val playheadUs: StateFlow<TimeUs> = _playheadUs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _previewAspectRatio = MutableStateFlow(initialProject.aspectRatio)
    val previewAspectRatio: StateFlow<AspectRatio> = _previewAspectRatio.asStateFlow()

    private var autoSaveJob: Job? = null

    fun update(transform: (Project) -> Project) {
        val current = _project.value
        val updated = transform(current)
        if (updated != current) {
            undoStack.add(current)
            if (undoStack.size > 50) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = false

            _project.value = updated
            scheduleAutoSave(updated)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_project.value)
            _project.value = previous
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            scheduleAutoSave(previous)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_project.value)
            _project.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            scheduleAutoSave(next)
        }
    }

    fun seekTo(timeUs: TimeUs) {
        val total = _project.value.durationUs.coerceAtLeast(1_000_000L)
        _playheadUs.value = timeUs.coerceIn(0L, total)
    }

    fun stepFrame(forward: Boolean) {
        val fps = _project.value.frameRate.coerceAtLeast(1)
        val frameDurationUs = 1_000_000L / fps
        val current = _playheadUs.value
        val next = if (forward) current + frameDurationUs else current - frameDurationUs
        seekTo(next)
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun setPreviewAspectRatio(aspectRatio: AspectRatio) {
        _previewAspectRatio.value = aspectRatio
    }

    private fun scheduleAutoSave(projectToSave: Project) {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(3000L) // Debounced 3s
            onAutoSave(projectToSave)
        }
    }

    fun flushSaveSync() {
        scope.launch {
            onAutoSave(_project.value)
        }
    }
}

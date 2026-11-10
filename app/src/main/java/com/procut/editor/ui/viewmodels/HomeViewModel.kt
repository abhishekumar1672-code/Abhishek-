package com.procut.editor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procut.editor.data.ProjectRepository
import com.procut.editor.model.AspectRatio
import com.procut.editor.model.Project
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    data class NavigateToEditor(val projectId: String) : HomeUiEvent()
    data class ShowToast(val message: String) : HomeUiEvent()
}

class HomeViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<Project>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<HomeUiEvent>()
    val events: SharedFlow<HomeUiEvent> = _events.asSharedFlow()

    // Dialog state for New Project
    var showNewProjectDialog = MutableStateFlow(false)
        private set

    // Selected project for long-press actions
    var actionProject = MutableStateFlow<Project?>(null)
        private set

    var showRenameDialog = MutableStateFlow(false)
        private set

    var showDeleteConfirmDialog = MutableStateFlow(false)
        private set

    fun openNewProjectDialog() {
        showNewProjectDialog.value = true
    }

    fun closeNewProjectDialog() {
        showNewProjectDialog.value = false
    }

    fun createProject(name: String, aspectRatio: AspectRatio, frameRate: Int) {
        viewModelScope.launch {
            val newProject = repository.createProject(name, aspectRatio, frameRate)
            showNewProjectDialog.value = false
            _events.emit(HomeUiEvent.NavigateToEditor(newProject.id))
        }
    }

    fun openActionMenu(project: Project) {
        actionProject.value = project
    }

    fun closeActionMenu() {
        actionProject.value = null
    }

    fun requestRename(project: Project) {
        actionProject.value = project
        showRenameDialog.value = true
    }

    fun confirmRename(newName: String) {
        val target = actionProject.value ?: return
        viewModelScope.launch {
            repository.renameProject(target.id, newName)
            showRenameDialog.value = false
            actionProject.value = null
        }
    }

    fun dismissRenameDialog() {
        showRenameDialog.value = false
        actionProject.value = null
    }

    fun requestDelete(project: Project) {
        actionProject.value = project
        showDeleteConfirmDialog.value = true
    }

    fun confirmDelete() {
        val target = actionProject.value ?: return
        viewModelScope.launch {
            repository.deleteProject(target.id)
            showDeleteConfirmDialog.value = false
            actionProject.value = null
            _events.emit(HomeUiEvent.ShowToast("Project deleted"))
        }
    }

    fun dismissDeleteDialog() {
        showDeleteConfirmDialog.value = false
        actionProject.value = null
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val duplicated = repository.duplicateProject(project.id)
            actionProject.value = null
            if (duplicated != null) {
                _events.emit(HomeUiEvent.ShowToast("Duplicated as ${duplicated.name}"))
            }
        }
    }

    fun saveAsTemplate(project: Project) {
        viewModelScope.launch {
            repository.saveAsTemplate(project.id)
            actionProject.value = null
            _events.emit(HomeUiEvent.ShowToast("Saved as template"))
        }
    }
}

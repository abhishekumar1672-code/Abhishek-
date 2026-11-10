package com.procut.editor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procut.editor.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val lowPowerMode: StateFlow<Boolean> = repository.lowPowerMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pixabayApiKey: StateFlow<String> = repository.pixabayApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val freesoundApiKey: StateFlow<String> = repository.freesoundApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val jamendoApiKey: StateFlow<String> = repository.jamendoApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setLowPowerMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setLowPowerMode(enabled)
        }
    }

    fun setPixabayKey(key: String) {
        viewModelScope.launch {
            repository.setPixabayApiKey(key)
        }
    }

    fun setFreesoundKey(key: String) {
        viewModelScope.launch {
            repository.setFreesoundApiKey(key)
        }
    }

    fun setJamendoKey(key: String) {
        viewModelScope.launch {
            repository.setJamendoApiKey(key)
        }
    }

    fun clearAllKeys() {
        viewModelScope.launch {
            repository.clearAllKeys()
        }
    }
}

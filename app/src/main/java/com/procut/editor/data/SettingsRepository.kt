package com.procut.editor.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val lowPowerMode: Flow<Boolean>
    val pixabayApiKey: Flow<String>
    val freesoundApiKey: Flow<String>
    val jamendoApiKey: Flow<String>

    suspend fun setLowPowerMode(enabled: Boolean)
    suspend fun setPixabayApiKey(key: String)
    suspend fun setFreesoundApiKey(key: String)
    suspend fun setJamendoApiKey(key: String)
    suspend fun clearAllKeys()
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "procut_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for JVM tests or hardware keystore unavailability
        context.getSharedPreferences("procut_fallback_prefs", Context.MODE_PRIVATE)
    }

    private val _lowPowerMode = MutableStateFlow(prefs.getBoolean(KEY_LOW_POWER, false))
    private val _pixabayKey = MutableStateFlow(prefs.getString(KEY_PIXABAY, "").orEmpty())
    private val _freesoundKey = MutableStateFlow(prefs.getString(KEY_FREESOUND, "").orEmpty())
    private val _jamendoKey = MutableStateFlow(prefs.getString(KEY_JAMENDO, "").orEmpty())

    override val lowPowerMode: Flow<Boolean> = _lowPowerMode.asStateFlow()
    override val pixabayApiKey: Flow<String> = _pixabayKey.asStateFlow()
    override val freesoundApiKey: Flow<String> = _freesoundKey.asStateFlow()
    override val jamendoApiKey: Flow<String> = _jamendoKey.asStateFlow()

    override suspend fun setLowPowerMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_POWER, enabled).apply()
        _lowPowerMode.value = enabled
    }

    override suspend fun setPixabayApiKey(key: String) {
        prefs.edit().putString(KEY_PIXABAY, key.trim()).apply()
        _pixabayKey.value = key.trim()
    }

    override suspend fun setFreesoundApiKey(key: String) {
        prefs.edit().putString(KEY_FREESOUND, key.trim()).apply()
        _freesoundKey.value = key.trim()
    }

    override suspend fun setJamendoApiKey(key: String) {
        prefs.edit().putString(KEY_JAMENDO, key.trim()).apply()
        _jamendoKey.value = key.trim()
    }

    override suspend fun clearAllKeys() {
        prefs.edit()
            .remove(KEY_PIXABAY)
            .remove(KEY_FREESOUND)
            .remove(KEY_JAMENDO)
            .apply()
        _pixabayKey.value = ""
        _freesoundKey.value = ""
        _jamendoKey.value = ""
    }

    companion object {
        private const val KEY_LOW_POWER = "key_low_power_mode"
        private const val KEY_PIXABAY = "key_pixabay_api"
        private const val KEY_FREESOUND = "key_freesound_api"
        private const val KEY_JAMENDO = "key_jamendo_api"
    }
}

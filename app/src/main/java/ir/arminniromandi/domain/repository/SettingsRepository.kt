package com.example.domain.repository

import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun updateSettings(update: (UserSettings) -> UserSettings)
}

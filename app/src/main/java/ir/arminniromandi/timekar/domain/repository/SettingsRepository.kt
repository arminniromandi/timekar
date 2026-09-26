package ir.arminniromandi.timekar.domain.repository

import ir.arminniromandi.timekar.domain.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun updateSettings(update: (UserSettings) -> UserSettings)
}

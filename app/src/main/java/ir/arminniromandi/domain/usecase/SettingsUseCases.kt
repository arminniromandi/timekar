package com.example.domain.usecase

import com.example.domain.model.UserSettings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<UserSettings> = repository.getSettings()
}

class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(update: (UserSettings) -> UserSettings) = repository.updateSettings(update)
}

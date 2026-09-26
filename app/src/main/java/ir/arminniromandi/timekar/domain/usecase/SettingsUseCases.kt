package ir.arminniromandi.timekar.domain.usecase

import ir.arminniromandi.timekar.domain.UserSettings
import ir.arminniromandi.timekar.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<UserSettings> = repository.getSettings()
}

class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(update: (UserSettings) -> UserSettings) = repository.updateSettings(update)
}

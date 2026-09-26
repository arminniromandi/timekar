package ir.arminniromandi.timekar.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.arminniromandi.timekar.domain.AccentColor
import ir.arminniromandi.timekar.domain.AppLanguage
import ir.arminniromandi.timekar.domain.StartDay
import ir.arminniromandi.timekar.domain.ThemeMode
import ir.arminniromandi.timekar.domain.UserSettings
import ir.arminniromandi.timekar.domain.usecase.GetSettingsUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<UserSettings> = getSettingsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            updateSettingsUseCase { current ->
                current.copy(
                    language = language,
                    userName = if (language == AppLanguage.PERSIAN) "آرش محمدی" else "Arash Mohammadi"
                )
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateSettingsUseCase { it.copy(themeMode = themeMode) }
        }
    }

    fun setAccentColor(accentColor: AccentColor) {
        viewModelScope.launch {
            updateSettingsUseCase { it.copy(accentColor = accentColor) }
        }
    }

    fun setStartDay(startDay: StartDay) {
        viewModelScope.launch {
            updateSettingsUseCase { it.copy(startDay = startDay) }
        }
    }

    fun toggleTaskReminders() {
        viewModelScope.launch {
            updateSettingsUseCase { it.copy(taskRemindersEnabled = !it.taskRemindersEnabled) }
        }
    }

    fun toggleDailyBriefing() {
        viewModelScope.launch {
            updateSettingsUseCase { it.copy(dailyBriefingEnabled = !it.dailyBriefingEnabled) }
        }
    }

    companion object {
        fun provideFactory(
            getSettingsUseCase: GetSettingsUseCase,
            updateSettingsUseCase: UpdateSettingsUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(getSettingsUseCase, updateSettingsUseCase) as T
            }
        }
    }
}

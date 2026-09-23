package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AccentColor
import com.example.domain.model.AppLanguage
import com.example.domain.model.StartDay
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserSettings
import com.example.domain.usecase.GetSettingsUseCase
import com.example.domain.usecase.UpdateSettingsUseCase
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

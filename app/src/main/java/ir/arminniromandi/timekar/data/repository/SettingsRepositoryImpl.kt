package ir.arminniromandi.timekar.data.repository

import android.content.Context
import android.content.SharedPreferences
import ir.arminniromandi.timekar.domain.AccentColor
import ir.arminniromandi.timekar.domain.AppLanguage
import ir.arminniromandi.timekar.domain.StartDay
import ir.arminniromandi.timekar.domain.ThemeMode
import ir.arminniromandi.timekar.domain.UserSettings

import ir.arminniromandi.timekar.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("chronos_user_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())

    override fun getSettings(): Flow<UserSettings> = _settings.asStateFlow()

    override suspend fun updateSettings(update: (UserSettings) -> UserSettings) {
        val newSettings = update(_settings.value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    private fun loadSettings(): UserSettings {
        val langCode = prefs.getString("language", AppLanguage.PERSIAN.code) ?: AppLanguage.PERSIAN.code
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val accentColorStr = prefs.getString("accent_color", AccentColor.EDITORIAL_BLUE.name) ?: AccentColor.EDITORIAL_BLUE.name
        val startDayStr = prefs.getString("start_day", StartDay.SATURDAY.name) ?: StartDay.SATURDAY.name
        val taskReminders = prefs.getBoolean("task_reminders", true)
        val dailyBriefing = prefs.getBoolean("daily_briefing", true)

        val language = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.PERSIAN
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (e: Exception) { ThemeMode.LIGHT }
        val accentColor = try { AccentColor.valueOf(accentColorStr) } catch (e: Exception) { AccentColor.EDITORIAL_BLUE }
        val startDay = try { StartDay.valueOf(startDayStr) } catch (e: Exception) { StartDay.SATURDAY }
//
        return UserSettings(
            language = language,
            themeMode = themeMode,
            accentColor = accentColor,
            startDay = startDay,
            taskRemindersEnabled = taskReminders,
            dailyBriefingEnabled = dailyBriefing,
            userName = if (language == AppLanguage.PERSIAN) "آرش محمدی" else "Arash Mohammadi",
            userEmail = "arash@example.com",
            isPro = true
        )
    }

    private fun saveSettings(settings: UserSettings) {
        prefs.edit()
            .putString("language", settings.language.code)
            .putString("theme_mode", settings.themeMode.name)
            .putString("accent_color", settings.accentColor.name)
            .putString("start_day", settings.startDay.name)
            .putBoolean("task_reminders", settings.taskRemindersEnabled)
            .putBoolean("daily_briefing", settings.dailyBriefingEnabled)
            .apply()
    }
}

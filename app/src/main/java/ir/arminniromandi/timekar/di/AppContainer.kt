package ir.arminniromandi.timekar.di

import android.content.Context
import ir.arminniromandi.timekar.data.local.AppDatabase
import ir.arminniromandi.timekar.data.repository.SettingsRepositoryImpl
import ir.arminniromandi.timekar.data.repository.TaskRepositoryImpl
import ir.arminniromandi.timekar.data.voice.VoiceToTextManager
import ir.arminniromandi.timekar.domain.repository.SettingsRepository
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import ir.arminniromandi.timekar.domain.usecase.AddTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.DeleteTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.GetSettingsUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksForDateUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleSubtaskUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleTaskCompleteUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateSettingsUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateTaskUseCase

interface AppContainer {
    val database: AppDatabase
    val taskRepository: TaskRepository
    val settingsRepository: SettingsRepository

    val getTasksUseCase: GetTasksUseCase
    val getTasksForDateUseCase: GetTasksForDateUseCase
    val addTaskUseCase: AddTaskUseCase
    val updateTaskUseCase: UpdateTaskUseCase
    val deleteTaskUseCase: DeleteTaskUseCase
    val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase
    val toggleSubtaskUseCase: ToggleSubtaskUseCase
    val getSettingsUseCase: GetSettingsUseCase
    val updateSettingsUseCase: UpdateSettingsUseCase

    val voiceManager: VoiceToTextManager

}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }


    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    override val getTasksUseCase: GetTasksUseCase by lazy {
        GetTasksUseCase(taskRepository)
    }

    override val getTasksForDateUseCase: GetTasksForDateUseCase by lazy {
        GetTasksForDateUseCase(taskRepository)
    }

    override val addTaskUseCase: AddTaskUseCase by lazy {
        AddTaskUseCase(taskRepository)
    }

    override val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(taskRepository)
    }

    override val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(taskRepository)
    }

    override val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase by lazy {
        ToggleTaskCompleteUseCase(taskRepository)
    }

    override val toggleSubtaskUseCase: ToggleSubtaskUseCase by lazy {
        ToggleSubtaskUseCase(taskRepository)
    }

    override val getSettingsUseCase: GetSettingsUseCase by lazy {
        GetSettingsUseCase(settingsRepository)
    }

    override val updateSettingsUseCase: UpdateSettingsUseCase by lazy {
        UpdateSettingsUseCase(settingsRepository)
    }
    override val voiceManager: VoiceToTextManager by lazy {
        VoiceToTextManager(context)
    }
}

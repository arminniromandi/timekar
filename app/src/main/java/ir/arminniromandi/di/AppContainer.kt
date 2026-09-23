package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.TaskRepository
import com.example.domain.usecase.AddTaskUseCase
import com.example.domain.usecase.DeleteTaskUseCase
import com.example.domain.usecase.GetSettingsUseCase
import com.example.domain.usecase.GetTasksForDateUseCase
import com.example.domain.usecase.GetTasksUseCase
import com.example.domain.usecase.ToggleSubtaskUseCase
import com.example.domain.usecase.ToggleTaskCompleteUseCase
import com.example.domain.usecase.UpdateSettingsUseCase
import com.example.domain.usecase.UpdateTaskUseCase

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
}

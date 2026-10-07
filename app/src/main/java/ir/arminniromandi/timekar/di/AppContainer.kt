package ir.arminniromandi.timekar.di

import android.content.Context
import ir.arminniromandi.timekar.data.local.AppDatabase
import ir.arminniromandi.timekar.data.remote.ApiService
import ir.arminniromandi.timekar.data.remote.NetworkModule
import ir.arminniromandi.timekar.data.repository.AiTaskRepository
import ir.arminniromandi.timekar.data.repository.SettingsRepositoryImpl
import ir.arminniromandi.timekar.data.repository.TaskRepositoryImpl
import ir.arminniromandi.timekar.data.voice.VoiceToTextManager
import ir.arminniromandi.timekar.domain.alarm.ReminderManager
import ir.arminniromandi.timekar.domain.alarm.TaskReminderScheduler
import ir.arminniromandi.timekar.domain.repository.SettingsRepository
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import ir.arminniromandi.timekar.domain.usecase.AddTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.DeleteTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.GetPendingTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.GetSettingsUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksForDateUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleSubtaskUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleTaskCompleteUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateSettingsUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateTaskUseCase
import ir.arminniromandi.timekar.framework.alarm.AndroidTaskReminderScheduler

/**
 * AppContainer interface — defines all dependencies available across the app.
 * Following Dependency Inversion Principle: consumers depend on this abstraction,
 * not on DefaultAppContainer directly.
 */
interface AppContainer {

    // --- Local storage ---
    val database: AppDatabase

    // --- Repositories ---
    val taskRepository: TaskRepository
    val settingsRepository: SettingsRepository
    val aiTaskRepository :AiTaskRepository

    // --- Alarm / Reminder ---
    val taskReminderScheduler: TaskReminderScheduler
    val reminderManager: ReminderManager

    // --- Task use cases ---
    val getTasksUseCase: GetTasksUseCase
    val getTasksForDateUseCase: GetTasksForDateUseCase
    val addTaskUseCase: AddTaskUseCase
    val updateTaskUseCase: UpdateTaskUseCase
    val deleteTaskUseCase: DeleteTaskUseCase
    val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase
    val toggleSubtaskUseCase: ToggleSubtaskUseCase
    val getPendingTask: GetPendingTaskUseCase

    // --- Settings use cases ---
    val getSettingsUseCase: GetSettingsUseCase
    val updateSettingsUseCase: UpdateSettingsUseCase

    // --- Voice ---
    val voiceManager: VoiceToTextManager

    // --- Network ---
    val apiService: ApiService
}

/**
 * Default implementation of AppContainer.
 * All dependencies are lazily initialized — created only when first accessed.
 * Following Single Responsibility Principle: each property is responsible for one dependency.
 * Following Open/Closed Principle: extend by adding new properties, not modifying existing ones.
 */
class DefaultAppContainer(
    private val context: Context
) : AppContainer {

    // -------------------------------------------------------------------------
    // Local storage
    // -------------------------------------------------------------------------

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    // -------------------------------------------------------------------------
    // Repositories
    // -------------------------------------------------------------------------

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    override val aiTaskRepository: AiTaskRepository by lazy {
        AiTaskRepository(apiService)
    }

    // -------------------------------------------------------------------------
    // Alarm / Reminder
    // -------------------------------------------------------------------------

    override val taskReminderScheduler: TaskReminderScheduler by lazy {
        AndroidTaskReminderScheduler(context)
    }

    override val reminderManager: ReminderManager by lazy {
        ReminderManager(
            scheduler = taskReminderScheduler,
            taskRepository = taskRepository,
            settingsRepository = settingsRepository
        )
    }

    // -------------------------------------------------------------------------
    // Task use cases
    // -------------------------------------------------------------------------

    override val getTasksUseCase: GetTasksUseCase by lazy {
        GetTasksUseCase(taskRepository)
    }

    override val getTasksForDateUseCase: GetTasksForDateUseCase by lazy {
        GetTasksForDateUseCase(taskRepository)
    }

    override val addTaskUseCase: AddTaskUseCase by lazy {
        AddTaskUseCase(
            repository = taskRepository,
            reminderManager = reminderManager
        )
    }

    override val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(
            repository = taskRepository,
            reminderManager = reminderManager
        )
    }

    override val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(
            repository = taskRepository,
            reminderManager = reminderManager
        )
    }

    override val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase by lazy {
        ToggleTaskCompleteUseCase(
            repository = taskRepository,
            reminderManager = reminderManager
        )
    }

    override val toggleSubtaskUseCase: ToggleSubtaskUseCase by lazy {
        ToggleSubtaskUseCase(taskRepository)
    }

    override val getPendingTask: GetPendingTaskUseCase by lazy {
        GetPendingTaskUseCase(taskRepository)
    }

    // -------------------------------------------------------------------------
    // Settings use cases
    // -------------------------------------------------------------------------

    override val getSettingsUseCase: GetSettingsUseCase by lazy {
        GetSettingsUseCase(settingsRepository)
    }

    override val updateSettingsUseCase: UpdateSettingsUseCase by lazy {
        UpdateSettingsUseCase(settingsRepository)
    }

    // -------------------------------------------------------------------------
    // Voice
    // -------------------------------------------------------------------------

    override val voiceManager: VoiceToTextManager by lazy {
        VoiceToTextManager(context)
    }

    // -------------------------------------------------------------------------
    // Network (Retrofit)
    // -------------------------------------------------------------------------



    private val loggingInterceptor by lazy {
        NetworkModule.provideLoggingInterceptor()
    }

    private val moshi by lazy {
        NetworkModule.provideMoshi()
    }

    private val okHttpClient by lazy {
        NetworkModule.provideOkHttpClient(
            loggingInterceptor = loggingInterceptor,)
    }

    private val retrofit by lazy {
        NetworkModule.provideRetrofit(
            okHttpClient = okHttpClient,
            moshi = moshi
        )
    }

    override val apiService: ApiService by lazy {
        NetworkModule.provideApiService(retrofit)
    }
}

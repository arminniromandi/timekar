package ir.arminniromandi.timekar.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.usecase.AddTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.DeleteTaskUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksForDateUseCase
import ir.arminniromandi.timekar.domain.usecase.GetTasksUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleSubtaskUseCase
import ir.arminniromandi.timekar.domain.usecase.ToggleTaskCompleteUseCase
import ir.arminniromandi.timekar.domain.usecase.UpdateTaskUseCase
import ir.arminniromandi.timekar.util.DateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SharedTasksUiState(
    val selectedEpochDay: Long = DateHelper.todayEpochDay(),
    val isNewTaskSheetVisible: Boolean = false,
    val taskToEdit: TaskItem? = null
)

class SharedTasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTasksForDateUseCase: GetTasksForDateUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedTasksUiState())
    val uiState: StateFlow<SharedTasksUiState> = _uiState.asStateFlow()

    /** روز انتخابی، به صورت یک جریان مستقل از بقیه‌ی فیلدها */
    val selectedEpochDayFlow: StateFlow<Long> = _uiState
        .map { it.selectedEpochDay }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _uiState.value.selectedEpochDay
        )

    /** کل وظایف، مشترک بین همه‌ی صفحه‌ها */
    val allTasks: StateFlow<List<TaskItem>> = getTasksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** وظایف روز انتخابی (برای تایم‌لاین و تقویم) */
    val selectedDayTasks: StateFlow<List<TaskItem>> = _uiState
        .map { it.selectedEpochDay }
        .distinctUntilChanged()
        .flatMapLatest { epochDay -> getTasksForDateUseCase(epochDay) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(epochDay: Long) {
        if (_uiState.value.selectedEpochDay == epochDay) return
        _uiState.value = _uiState.value.copy(selectedEpochDay = epochDay)
    }

    fun goToToday() {
        selectDate(DateHelper.todayEpochDay())
    }

    fun openNewTaskSheet(taskToEdit: TaskItem? = null) {
        _uiState.value = _uiState.value.copy(
            isNewTaskSheetVisible = true,
            taskToEdit = taskToEdit
        )
    }

    fun closeNewTaskSheet() {
        _uiState.value = _uiState.value.copy(
            isNewTaskSheetVisible = false,
            taskToEdit = null
        )
    }

    fun saveTask(task: TaskItem) {
        viewModelScope.launch {
            if (task.id == 0L) {
                addTaskUseCase(task)
            } else {
                updateTaskUseCase(task)
            }
            closeNewTaskSheet()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    fun toggleTaskComplete(taskId: Long) {
        viewModelScope.launch {
            toggleTaskCompleteUseCase(taskId)
        }
    }

    fun toggleSubtask(taskId: Long, subtaskId: String) {
        viewModelScope.launch {
            toggleSubtaskUseCase(taskId, subtaskId)
        }
    }

    companion object {
        fun provideFactory(
            getTasksUseCase: GetTasksUseCase,
            getTasksForDateUseCase: GetTasksForDateUseCase,
            addTaskUseCase: AddTaskUseCase,
            updateTaskUseCase: UpdateTaskUseCase,
            deleteTaskUseCase: DeleteTaskUseCase,
            toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
            toggleSubtaskUseCase: ToggleSubtaskUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SharedTasksViewModel(
                    getTasksUseCase,
                    getTasksForDateUseCase,
                    addTaskUseCase,
                    updateTaskUseCase,
                    deleteTaskUseCase,
                    toggleTaskCompleteUseCase,
                    toggleSubtaskUseCase
                ) as T
            }
        }
    }
}

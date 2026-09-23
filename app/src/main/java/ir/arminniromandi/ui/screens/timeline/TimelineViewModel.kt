package com.example.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TaskItem
import com.example.domain.usecase.AddTaskUseCase
import com.example.domain.usecase.GetTasksForDateUseCase
import com.example.domain.usecase.GetTasksUseCase
import com.example.domain.usecase.ToggleSubtaskUseCase
import com.example.domain.usecase.ToggleTaskCompleteUseCase
import com.example.domain.usecase.UpdateTaskUseCase
import com.example.util.DateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimelineUiState(
    val selectedEpochDay: Long = DateHelper.todayEpochDay(),
    val isNewTaskSheetVisible: Boolean = false,
    val taskToEdit: TaskItem? = null
)

open class TimelineViewModel(
    private val getTasksForDateUseCase: GetTasksForDateUseCase,
    private val getAllTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    val dayTasks: StateFlow<List<TaskItem>> = _uiState
        .flatMapLatest { state ->
            getTasksForDateUseCase(state.selectedEpochDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(epochDay: Long) {
        _uiState.value = _uiState.value.copy(selectedEpochDay = epochDay)
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
            getTasksForDateUseCase: GetTasksForDateUseCase,
            getAllTasksUseCase: GetTasksUseCase,
            addTaskUseCase: AddTaskUseCase,
            updateTaskUseCase: UpdateTaskUseCase,
            toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
            toggleSubtaskUseCase: ToggleSubtaskUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimelineViewModel(
                    getTasksForDateUseCase,
                    getAllTasksUseCase,
                    addTaskUseCase,
                    updateTaskUseCase,
                    toggleTaskCompleteUseCase,
                    toggleSubtaskUseCase
                ) as T
            }
        }
    }
}

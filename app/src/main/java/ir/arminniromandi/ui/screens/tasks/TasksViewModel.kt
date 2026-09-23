package com.example.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TaskItem
import com.example.domain.usecase.AddTaskUseCase
import com.example.domain.usecase.DeleteTaskUseCase
import com.example.domain.usecase.GetTasksUseCase
import com.example.domain.usecase.ToggleSubtaskUseCase
import com.example.domain.usecase.ToggleTaskCompleteUseCase
import com.example.domain.usecase.UpdateTaskUseCase
import com.example.util.DateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, TODAY, UPCOMING, OVERDUE, COMPLETED
}

data class TasksUiState(
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isCompletedSectionExpanded: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isNewTaskSheetVisible: Boolean = false,
    val taskToEdit: TaskItem? = null
)

class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val allTasksFlow = getTasksUseCase()

    val allTasks: StateFlow<List<TaskItem>> = allTasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTasks: StateFlow<List<TaskItem>> = combine(allTasksFlow, _uiState) { tasks, state ->
        val todayEpoch = DateHelper.todayEpochDay()
        var list = tasks

        if (state.searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(state.searchQuery, ignoreCase = true) ||
                it.description.contains(state.searchQuery, ignoreCase = true)
            }
        }

        when (state.selectedFilter) {
            TaskFilter.ALL -> list
            TaskFilter.TODAY -> list.filter { it.dateEpochDay == todayEpoch }
            TaskFilter.UPCOMING -> list.filter { it.dateEpochDay > todayEpoch && !it.isCompleted }
            TaskFilter.OVERDUE -> list.filter { it.dateEpochDay < todayEpoch && !it.isCompleted }
            TaskFilter.COMPLETED -> list.filter { it.isCompleted }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: TaskFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun toggleCompletedSection() {
        _uiState.value = _uiState.value.copy(
            isCompletedSectionExpanded = !_uiState.value.isCompletedSectionExpanded
        )
    }

    fun toggleSearch() {
        _uiState.value = _uiState.value.copy(
            isSearchActive = !_uiState.value.isSearchActive,
            searchQuery = if (_uiState.value.isSearchActive) "" else _uiState.value.searchQuery
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
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
            getTasksUseCase: GetTasksUseCase,
            addTaskUseCase: AddTaskUseCase,
            updateTaskUseCase: UpdateTaskUseCase,
            deleteTaskUseCase: DeleteTaskUseCase,
            toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
            toggleSubtaskUseCase: ToggleSubtaskUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TasksViewModel(
                    getTasksUseCase,
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

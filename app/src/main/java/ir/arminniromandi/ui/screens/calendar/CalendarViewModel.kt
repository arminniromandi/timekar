package com.example.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TaskItem
import com.example.domain.usecase.AddTaskUseCase
import com.example.domain.usecase.GetTasksUseCase
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
import java.util.Calendar

data class CalendarUiState(
    val selectedYear: Int = 2024,
    val selectedMonth: Int = 10, // 1-based (1..12)
    val selectedEpochDay: Long = DateHelper.todayEpochDay(),
    val isNewTaskSheetVisible: Boolean = false,
    val taskToEdit: TaskItem? = null
)

class CalendarViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(initUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val allTasksFlow = getTasksUseCase()

    val allTasks: StateFlow<List<TaskItem>> = allTasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedDayTasks: StateFlow<List<TaskItem>> = combine(allTasksFlow, _uiState) { tasks, state ->
        tasks.filter { it.dateEpochDay == state.selectedEpochDay }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun initUiState(): CalendarUiState {
        val cal = Calendar.getInstance()
        return CalendarUiState(
            selectedYear = cal.get(Calendar.YEAR),
            selectedMonth = cal.get(Calendar.MONTH) + 1,
            selectedEpochDay = DateHelper.todayEpochDay()
        )
    }

    fun selectDate(epochDay: Long) {
        val cal = DateHelper.getCalendarForEpochDay(epochDay)
        _uiState.value = _uiState.value.copy(
            selectedEpochDay = epochDay,
            selectedYear = cal.get(Calendar.YEAR),
            selectedMonth = cal.get(Calendar.MONTH) + 1
        )
    }

    fun goToToday() {
        selectDate(DateHelper.todayEpochDay())
    }

    fun previousMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        if (currentMonth == 1) {
            _uiState.value = _uiState.value.copy(selectedMonth = 12, selectedYear = currentYear - 1)
        } else {
            _uiState.value = _uiState.value.copy(selectedMonth = currentMonth - 1)
        }
    }

    fun nextMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        if (currentMonth == 12) {
            _uiState.value = _uiState.value.copy(selectedMonth = 1, selectedYear = currentYear + 1)
        } else {
            _uiState.value = _uiState.value.copy(selectedMonth = currentMonth + 1)
        }
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

    companion object {
        fun provideFactory(
            getTasksUseCase: GetTasksUseCase,
            addTaskUseCase: AddTaskUseCase,
            updateTaskUseCase: UpdateTaskUseCase,
            toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalendarViewModel(
                    getTasksUseCase,
                    addTaskUseCase,
                    updateTaskUseCase,
                    toggleTaskCompleteUseCase
                ) as T
            }
        }
    }
}

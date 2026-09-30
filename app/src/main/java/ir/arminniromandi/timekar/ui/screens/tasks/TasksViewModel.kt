package ir.arminniromandi.timekar.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.ui.shared.SharedTasksViewModel
import ir.arminniromandi.timekar.util.DateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TaskFilter {
    ALL, TODAY, UPCOMING, OVERDUE, COMPLETED
}

data class TasksUiState(
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isCompletedSectionExpanded: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

class TasksViewModel(
    private val sharedViewModel: SharedTasksViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    val allTasks: StateFlow<List<TaskItem>> = sharedViewModel.allTasks

    val filteredTasks: StateFlow<List<TaskItem>> =
        combine(sharedViewModel.allTasks, _uiState) { tasks, state ->
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

    companion object {
        fun provideFactory(
            sharedViewModel: SharedTasksViewModel
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TasksViewModel(sharedViewModel) as T
            }
        }
    }
}

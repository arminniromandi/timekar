package ir.arminniromandi.timekar.ui.screens.calendar

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar


data class CalendarUiState(
    val selectedYear: Int = 2024,
    val selectedMonth: Int = 10 // 1-based (1..12)
)

class CalendarViewModel(
    private val sharedViewModel: SharedTasksViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(initMonthState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    /** روز انتخابی، از ویومدل مشترک خوانده می‌شود */
    val selectedEpochDay: StateFlow<Long> = sharedViewModel.uiState
        .map { it.selectedEpochDay }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = sharedViewModel.uiState.value.selectedEpochDay
        )

    val allTasks: StateFlow<List<TaskItem>> = sharedViewModel.allTasks

    val selectedDayTasks: StateFlow<List<TaskItem>> = sharedViewModel.selectedDayTasks

    private fun initMonthState(): CalendarUiState {
        val cal = Calendar.getInstance()
        return CalendarUiState(
            selectedYear = cal.get(Calendar.YEAR),
            selectedMonth = cal.get(Calendar.MONTH) + 1
        )
    }

    /**
     * انتخاب روز؛ روز به‌صورت مشترک ذخیره می‌شود و ماه/سال نمایشی هم با آن هماهنگ می‌شود.
     */
    fun selectDate(epochDay: Long, isPersian: Boolean) {
        sharedViewModel.selectDate(epochDay)
        syncMonthWithSelectedDate(epochDay, isPersian)
    }

    fun goToToday(isPersian: Boolean) {
        val todayEpoch = DateHelper.todayEpochDay()
        sharedViewModel.selectDate(todayEpoch)
        syncMonthWithSelectedDate(todayEpoch, isPersian)
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

    /**
     * وقتی روز انتخابی از صفحات دیگر (تایم‌لاین) تغییر کرد، ماه نمایشی هم دنبالش می‌رود.
     */
    fun syncMonthWithSelectedDate(epochDay: Long, isPersian: Boolean) {
        val cal = DateHelper.getCalendarForEpochDay(epochDay)
        if (isPersian) {
            val pDate = DateHelper.gregorianToPersian(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            _uiState.value = _uiState.value.copy(
                selectedYear = pDate.year,
                selectedMonth = pDate.month
            )
        } else {
            _uiState.value = _uiState.value.copy(
                selectedYear = cal.get(Calendar.YEAR),
                selectedMonth = cal.get(Calendar.MONTH) + 1
            )
        }
    }

    companion object {
        fun provideFactory(
            sharedViewModel: SharedTasksViewModel
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalendarViewModel(sharedViewModel) as T
            }
        }
    }
}

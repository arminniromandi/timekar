package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
import com.example.ui.components.ChronosBottomNavBar
import com.example.ui.components.NavTab
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.calendar.CalendarViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.tasks.TasksViewModel
import com.example.ui.screens.timeline.TimelineScreen
import com.example.ui.screens.timeline.TimelineViewModel
import com.example.ui.strings.AppStrings
import com.example.ui.theme.ChronosTheme

@Composable
fun MainScreen(
    container: AppContainer
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            container.getSettingsUseCase,
            container.updateSettingsUseCase
        )
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val strings = remember(settings.language) { AppStrings(settings.language) }

    ChronosTheme(settings = settings) {
        var currentTab by remember { mutableStateOf(NavTab.TIMELINE) }

        val timelineViewModel: TimelineViewModel = viewModel(
            factory = TimelineViewModel.provideFactory(
                container.getTasksForDateUseCase,
                container.getTasksUseCase,
                container.addTaskUseCase,
                container.updateTaskUseCase,
                container.toggleTaskCompleteUseCase,
                container.toggleSubtaskUseCase
            )
        )

        val tasksViewModel: TasksViewModel = viewModel(
            factory = TasksViewModel.provideFactory(
                container.getTasksUseCase,
                container.addTaskUseCase,
                container.updateTaskUseCase,
                container.deleteTaskUseCase,
                container.toggleTaskCompleteUseCase,
                container.toggleSubtaskUseCase
            )
        )

        val calendarViewModel: CalendarViewModel = viewModel(
            factory = CalendarViewModel.provideFactory(
                container.getTasksUseCase,
                container.addTaskUseCase,
                container.updateTaskUseCase,
                container.toggleTaskCompleteUseCase
            )
        )


        Scaffold(
            bottomBar = {
                ChronosBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    strings = strings
                )
            },
            modifier = Modifier.fillMaxSize()

        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.TIMELINE -> TimelineScreen(
                        viewModel = timelineViewModel,
                        strings = strings,
                        onNavigateToCalendar = { currentTab = NavTab.CALENDAR }
                    )
                    NavTab.TASKS -> TasksScreen(
                        viewModel = tasksViewModel,
                        strings = strings
                    )
                    NavTab.CALENDAR -> CalendarScreen(
                        viewModel = calendarViewModel,
                        strings = strings
                    )
                    NavTab.SETTINGS -> SettingsScreen(
                        viewModel = settingsViewModel,
                        strings = strings
                    )
                }
            }
        }
    }
}

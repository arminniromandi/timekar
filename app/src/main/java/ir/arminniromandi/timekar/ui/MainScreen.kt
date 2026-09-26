package ir.arminniromandi.timekar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.arminniromandi.timekar.di.AppContainer
import ir.arminniromandi.timekar.ui.components.ChronosBottomNavBar
import ir.arminniromandi.timekar.ui.components.NavTab
import ir.arminniromandi.timekar.ui.screens.calendar.CalendarScreen
import ir.arminniromandi.timekar.ui.screens.calendar.CalendarViewModel
import ir.arminniromandi.timekar.ui.screens.settings.SettingsScreen
import ir.arminniromandi.timekar.ui.screens.settings.SettingsViewModel
import ir.arminniromandi.timekar.ui.screens.tasks.TasksScreen
import ir.arminniromandi.timekar.ui.screens.tasks.TasksViewModel
import ir.arminniromandi.timekar.ui.screens.timeline.TimelineScreen
import ir.arminniromandi.timekar.ui.screens.timeline.TimelineViewModel
import ir.arminniromandi.timekar.ui.strings.AppStrings
import ir.arminniromandi.timekar.ui.theme.ChronosTheme

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
            factory = TasksViewModel.Companion.provideFactory(
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
                        onNavigateToCalendar = { currentTab = NavTab.CALENDAR },
                        userSettings = settings
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

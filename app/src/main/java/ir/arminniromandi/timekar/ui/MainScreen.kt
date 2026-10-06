package ir.arminniromandi.timekar.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.arminniromandi.timekar.data.voice.VoiceToTextManager
import ir.arminniromandi.timekar.di.AppContainer
import ir.arminniromandi.timekar.ui.components.ChronosBottomNavBar
import ir.arminniromandi.timekar.ui.components.NavTab
import ir.arminniromandi.timekar.ui.components.SpeedDialFab
import ir.arminniromandi.timekar.ui.components.permissionChecker.AlarmPermissionChecker
import ir.arminniromandi.timekar.ui.components.permissionChecker.RecordAudioPermissionChecker
import ir.arminniromandi.timekar.ui.screens.calendar.CalendarScreen
import ir.arminniromandi.timekar.ui.screens.calendar.CalendarViewModel
import ir.arminniromandi.timekar.ui.screens.settings.SettingsScreen
import ir.arminniromandi.timekar.ui.screens.settings.SettingsViewModel
import ir.arminniromandi.timekar.ui.screens.tasks.TasksScreen
import ir.arminniromandi.timekar.ui.screens.tasks.TasksViewModel
import ir.arminniromandi.timekar.ui.screens.timeline.TimelineScreen
import ir.arminniromandi.timekar.ui.shared.SharedTasksViewModel
import ir.arminniromandi.timekar.ui.strings.AppStrings
import ir.arminniromandi.timekar.ui.theme.ChronosTheme
import ir.arminniromandi.timekar.ui.voice.VoiceDialogHandler
import ir.arminniromandi.timekar.ui.voice.VoiceEventHandler
import ir.arminniromandi.timekar.ui.voice.VoiceTaskViewModel

@Composable
fun MainScreen(
    container: AppContainer
) {
    val context = LocalContext.current
    
    // Settings ViewModel
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            container.getSettingsUseCase,
            container.updateSettingsUseCase,
            container.reminderManager
        )
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val strings = remember(settings.language) { AppStrings(settings.language) }


    // Shared Tasks ViewModel
    val sharedTasksViewModel: SharedTasksViewModel = viewModel(
        factory = SharedTasksViewModel.provideFactory(
            container.getTasksUseCase,
            container.getTasksForDateUseCase,
            container.addTaskUseCase,
            container.updateTaskUseCase,
            container.deleteTaskUseCase,
            container.toggleTaskCompleteUseCase,
            container.toggleSubtaskUseCase
        )
    )

    // Voice ViewModel
    val voiceManager = remember { VoiceToTextManager(context) }
    val voiceViewModel: VoiceTaskViewModel = viewModel(
        factory = VoiceTaskViewModel.provideFactory(voiceManager)
    )

    // Voice Dialog State
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Permission Handlers
    AlarmPermissionChecker(context)
    val checkAndRequestAudioPermission = RecordAudioPermissionChecker(
        onPermissionGranted = { showVoiceDialog = true }
    )

    // Voice Event Handler
    VoiceEventHandler(
        context = context,
        showVoiceDialog = showVoiceDialog,
        language = settings.language,
        voiceViewModel = voiceViewModel,
        sharedViewModel = sharedTasksViewModel
    )

    ChronosTheme(settings = settings) {
        var currentTab by remember { mutableStateOf(NavTab.TIMELINE) }

        val tasksViewModel: TasksViewModel = viewModel(
            factory = TasksViewModel.provideFactory(sharedTasksViewModel)
        )

        val calendarViewModel: CalendarViewModel = viewModel(
            factory = CalendarViewModel.provideFactory(sharedTasksViewModel)
        )

        Scaffold(
            bottomBar = {
                ChronosBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    strings = strings
                )
            },
            floatingActionButton = {
                if (currentTab != NavTab.SETTINGS)
                    SpeedDialFab(
                        onManualTaskClick = { sharedTasksViewModel.openNewTaskSheet(null) },
                        onVoiceTaskClick = { checkAndRequestAudioPermission() },
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
                // Voice Dialog
                VoiceDialogHandler(
                    showDialog = showVoiceDialog,
                    voiceViewModel = voiceViewModel,
                    sharedViewModel = sharedTasksViewModel,
                    strings = strings,
                    onDismiss = { showVoiceDialog = false }
                )

                // Main Content
                when (currentTab) {
                    NavTab.TIMELINE -> TimelineScreen(
                        viewModel = sharedTasksViewModel,
                        strings = strings,
                        onNavigateToCalendar = { currentTab = NavTab.CALENDAR },
                        userSettings = settings
                    )

                    NavTab.TASKS -> TasksScreen(
                        viewModel = tasksViewModel,
                        sharedViewModel = sharedTasksViewModel,
                        strings = strings
                    )

                    NavTab.CALENDAR -> CalendarScreen(
                        viewModel = calendarViewModel,
                        sharedViewModel = sharedTasksViewModel,
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

package ir.arminniromandi.timekar.ui.screens.timeline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.arminniromandi.timekar.domain.AppLanguage
import ir.arminniromandi.timekar.domain.StartDay
import ir.arminniromandi.timekar.domain.UserSettings
import ir.arminniromandi.timekar.domain.model.Priority
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.ui.components.NewTaskBottomSheet
import ir.arminniromandi.timekar.ui.components.SaveTaskFromVoiceDialog
import ir.arminniromandi.timekar.ui.components.SpeedDialFab
import ir.arminniromandi.timekar.ui.components.TimelineHeader
import ir.arminniromandi.timekar.ui.strings.AppStrings
import ir.arminniromandi.timekar.util.DateHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

private const val PAGER_PAGE_COUNT = 100_000
private const val PAGER_INITIAL_PAGE = PAGER_PAGE_COUNT / 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    strings: AppStrings,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    userSettings: UserSettings = UserSettings()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.dayTasks.collectAsStateWithLifecycle()

    TimelineContent(
        uiState = uiState,
        tasks = tasks,
        strings = strings,
        startDay = userSettings.startDay,
        onNavigateToCalendar = onNavigateToCalendar,
        onSelectDate = { viewModel.selectDate(it) },
        onOpenNewTaskSheet = { viewModel.openNewTaskSheet(it) },
        onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
        onCloseNewTaskSheet = { viewModel.closeNewTaskSheet() },
        onSaveTask = { viewModel.saveTask(it) },
        modifier = modifier
    )
}



@Composable
fun TimelineContent(
    uiState: TimelineUiState,
    tasks: List<TaskItem>,
    strings: AppStrings,
    startDay: StartDay = StartDay.SATURDAY,
    onNavigateToCalendar: () -> Unit,
    onSelectDate: (Long) -> Unit,
    onOpenNewTaskSheet: (TaskItem?) -> Unit,
    onToggleTaskComplete: (Long) -> Unit,
    onCloseNewTaskSheet: () -> Unit,
    onSaveTask: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val todayEpoch = remember { DateHelper.todayEpochDay() }



    fun pageToEpoch(page: Int): Long = todayEpoch + (page - PAGER_INITIAL_PAGE)
    fun epochToPage(epoch: Long): Int = PAGER_INITIAL_PAGE + (epoch - todayEpoch).toInt()




    val pagerState = rememberPagerState(
        initialPage = remember { epochToPage(uiState.selectedEpochDay) },
        pageCount = { PAGER_PAGE_COUNT }
    )

    val latestSelectedEpochDay by rememberUpdatedState(uiState.selectedEpochDay)

// ۱. هماهنگی اسکرول کاربر با تغییر تاریخ در ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            val targetEpoch = pageToEpoch(settledPage)
            if (targetEpoch != latestSelectedEpochDay) {
                onSelectDate(targetEpoch)
            }
        }
    }

// ۲. انیمیشن اسکرول فقط زمانی فعال شود که کاربر خودش صفحه را با دست نگه نداشته باشد
    LaunchedEffect(uiState.selectedEpochDay) {
        val targetPage = epochToPage(uiState.selectedEpochDay)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val dateHeaderLabel = remember(uiState.selectedEpochDay, strings.isPersian) {
        if (strings.isPersian) {
            DateHelper.formatPersianHeaderDate(uiState.selectedEpochDay)
        } else {
            DateHelper.formatEnglishHeaderDate(uiState.selectedEpochDay)
        }
    }

    Scaffold(
        topBar = {
            TimelineHeader(
                dateLabel = dateHeaderLabel,
                strings = strings,
                onTodayClick = {
                    onSelectDate(todayEpoch)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(epochToPage(todayEpoch))
                    }
                },
                onMonthViewClick = onNavigateToCalendar
            )
        },
        floatingActionButton = {



        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // نوار ثابت هفتگی بر اساس روز شروع تنظیمات (startDay)
            WeeklyDateStrip(
                selectedEpochDay = uiState.selectedEpochDay,
                todayEpochDay = todayEpoch,
                startDay = startDay,
                isPersian = strings.isPersian,
                onSelectDate = { epochDay ->
                    onSelectDate(epochDay)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(epochToPage(epochDay))
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // پیجر افقی برای روزها
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageEpochDay = pageToEpoch(page)
                val isSelectedDay = pageEpochDay == uiState.selectedEpochDay
                val dayTasks = if (isSelectedDay) tasks else emptyList()

                val anytimeTasks = remember(dayTasks) {
                    dayTasks.filter { it.isAllDay || it.startTimeMinute < 0 }
                }

                val timedTasks = remember(dayTasks) {
                    dayTasks.filter { !it.isAllDay && it.startTimeMinute >= 0 }
                        .sortedBy { it.startTimeMinute }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    if (anytimeTasks.isNotEmpty()) {
                        AnytimeTasksSection(
                            tasks = anytimeTasks,
                            strings = strings,
                            onOpenNewTaskSheet = onOpenNewTaskSheet,
                            onToggleTaskComplete = onToggleTaskComplete
                        )
                    }

                    TimelineGrid(
                        tasks = timedTasks,
                        strings = strings,
                        onTaskClick = { onOpenNewTaskSheet(it) },
                        onToggleComplete = onToggleTaskComplete,
                        selectedEpochDay = pageEpochDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }
        }

        if (uiState.isNewTaskSheetVisible) {
            NewTaskBottomSheet(
                strings = strings,
                initialTask = uiState.taskToEdit,
                onDismiss = onCloseNewTaskSheet,
                onSaveTask = onSaveTask
            )
        }
    }
}

@Composable
private fun WeeklyDateStrip(
    selectedEpochDay: Long,
    todayEpochDay: Long,
    startDay: StartDay,
    isPersian: Boolean,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // اندیس روز شروع بر اساس شنبه (0 = شنبه تا 6 = جمعه)
    val startDayIndex = when (startDay) {
        StartDay.SATURDAY -> 0
        StartDay.SUNDAY -> 1
        StartDay.MONDAY -> 2
        else -> 0
    }

    // محاسبه دقیق و ریاضی ۷ روز هفته، کاملاً ثابت برای تمام روزهای داخل همان هفته
    val days = remember(selectedEpochDay, startDayIndex) {
        // تبدیل فرمول ریاضی قطعی: روز 0 مبدا یونیکس پنج‌شنبه بوده است
        val dayOfWeekFromSat = Math.floorMod(selectedEpochDay + 5L, 7L).toInt()
        val daysSinceWeekStart = Math.floorMod(dayOfWeekFromSat - startDayIndex, 7)
        val startOfWeekEpoch = selectedEpochDay - daysSinceWeekStart
        (0..6).map { startOfWeekEpoch + it }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { epochDay ->
                val isSelected = epochDay == selectedEpochDay
                val isToday = epochDay == todayEpochDay

                val dayOfWeekFromSat = Math.floorMod(epochDay + 5L, 7L).toInt()

                val dayLabel = if (isPersian) {
                    when (dayOfWeekFromSat) {
                        0 -> "ش"
                        1 -> "ی"
                        2 -> "د"
                        3 -> "س"
                        4 -> "چ"
                        5 -> "پ"
                        else -> "ج"
                    }
                } else {
                    when (dayOfWeekFromSat) {
                        0 -> "S"
                        1 -> "S"
                        2 -> "M"
                        3 -> "T"
                        4 -> "W"
                        5 -> "T"
                        else -> "F"
                    }
                }

                val shamsiDay = DateHelper.epochDayToPersianDirect(epochDay)
                val cal = DateHelper.getCalendarForEpochDay(epochDay)
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                val dayNumStr = if (isPersian) DateHelper.formatPersianNumber(shamsiDay.day) else dayOfMonth.toString()

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onSurface
                                else -> Color.Transparent
                            }
                        )
                        .then(
                            // اگر امروز بود اما انتخاب نشده بود، یک کادر ملایم دور آن می‌افتد
                            if (isToday && !isSelected) {
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onSelectDate(epochDay) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.surfaceContainerHighest
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                    Text(
                        text = dayNumStr,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.surfaceContainerLowest
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // نشانگر کوچک زیر عدد در صورتی که روز جاری (امروز) باشد
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnytimeTasksSection(
    tasks: List<TaskItem>,
    strings: AppStrings,
    onOpenNewTaskSheet: (TaskItem) -> Unit,
    onToggleTaskComplete: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.anytime,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${
                    if (strings.isPersian) DateHelper.formatPersianNumber(tasks.size) else tasks.size
                } ${strings.tasksCountSuffix}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                tasks.forEachIndexed { index, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNewTaskSheet(task) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable { onToggleTaskComplete(task.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = if (strings.isPersian) task.category.persianLabel else task.category.englishLabel,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                    if (index < tasks.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                alpha = 0.5f
                            ),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineGrid(
    tasks: List<TaskItem>,
    strings: AppStrings,
    selectedEpochDay: Long,
    onTaskClick: (TaskItem) -> Unit,
    onToggleComplete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (0..23).toList() }

    val isToday = remember(selectedEpochDay) {
        selectedEpochDay == DateHelper.todayEpochDay()
    }

    val calendar = remember { Calendar.getInstance() }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    Column(modifier = modifier) {
        hours.forEach { hour ->
            val hourTasks = tasks.filter { !it.isAllDay && (it.startTimeMinute / 60) == hour }
                .sortedWith(
                    compareBy<TaskItem> { it.startTimeMinute }
                        .thenBy { it.endTimeMinute }
                        .thenBy { it.createdAt }
                )
            val hourLabel = if (strings.isPersian) {
                "${DateHelper.formatPersianNumber(hour)}:۰۰"
            } else {
                String.format("%02d:00", hour)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hourLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(48.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            thickness = 1.dp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 56.dp,
                                top = 6.dp,
                                bottom = 6.dp
                            )
                    ) {
                        if (hourTasks.isNotEmpty()) {
                            hourTasks.forEach { task ->
                                TimelineTaskCard(
                                    task = task,
                                    strings = strings,
                                    onClick = { onTaskClick(task) },
                                    onToggleComplete = { onToggleComplete(task.id) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

                if (isToday && hour == currentHour) {
                    val minuteFraction = currentMinute / 60f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (minuteFraction * 64).dp)
                    ) {
                        NowIndicatorLine(
                            hour = currentHour,
                            minute = currentMinute,
                            strings = strings
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowIndicatorLine(
    hour: Int,
    minute: Int,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val nowTime = if (strings.isPersian) {
        val minuteStr =
            if (minute < 10) "۰${DateHelper.formatPersianNumber(minute)}" else DateHelper.formatPersianNumber(
                minute
            )
        "${DateHelper.formatPersianNumber(hour)}:$minuteStr"
    } else {
        String.format("%02d:%02d", hour, minute)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = nowTime,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.5.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun TimelineTaskCard(
    task: TaskItem,
    strings: AppStrings,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val priorityDotColor = when (task.priority) {
        Priority.HIGH -> Color(Priority.HIGH.colorHex)
        Priority.MEDIUM -> Color(Priority.MEDIUM.colorHex)
        Priority.NORMAL -> MaterialTheme.colorScheme.secondary
        Priority.LOW -> MaterialTheme.colorScheme.outline
    }

    if (task.isCompleted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Text(
                text = strings.done,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(priorityDotColor)
                    )

                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = task.formattedTimeSlot,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            if (task.locationOrDetails.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Text(
                                    text = task.locationOrDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                Text(
                                    text = if (strings.isPersian) task.category.persianLabel else task.category.englishLabel,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }

                            if (task.priority == Priority.HIGH) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = if (strings.isPersian) "اولویت بالا" else "Priority",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 5.dp,
                                            vertical = 1.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                when (task.iconType) {
                    "group" -> Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )

                    "call" -> Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )

                    "edit_note" -> Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )

                    "meet" -> Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (strings.isPersian) "میت" else "Meet",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TimelineGridPreviewData(
    val tasks: List<TaskItem>,
    val strings: AppStrings,
    val selectedEpochDay: Long
)

class TimelineGridPreviewParameterProvider : PreviewParameterProvider<TimelineGridPreviewData> {
    @RequiresApi(Build.VERSION_CODES.O)
    private val todayEpoch = LocalDate.now().toEpochDay()

    @RequiresApi(Build.VERSION_CODES.O)
    override val values: Sequence<TimelineGridPreviewData> = sequenceOf(
        TimelineGridPreviewData(
            tasks = listOf(
                TaskItem(
                    id = 1L,
                    title = "بررسی ایمیل‌ها و پلن روزانه",
                    dateEpochDay = todayEpoch,
                    isAllDay = true,
                    isCompleted = true
                )
            ),
            strings = AppStrings(AppLanguage.ENGLISH),
            selectedEpochDay = todayEpoch
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun TimelineGridPreview(
    @PreviewParameter(TimelineGridPreviewParameterProvider::class) data: TimelineGridPreviewData
) {
    TimelineGrid(
        tasks = data.tasks,
        strings = data.strings,
        selectedEpochDay = data.selectedEpochDay,
        onTaskClick = {},
        onToggleComplete = {},
        modifier = Modifier
    )
}
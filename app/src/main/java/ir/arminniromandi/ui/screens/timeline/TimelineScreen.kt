package com.example.ui.screens.timeline

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppLanguage
import com.example.domain.model.Priority
import com.example.domain.model.TaskItem
import com.example.ui.components.NewTaskBottomSheet
import com.example.ui.components.TimelineHeader
import com.example.ui.strings.AppStrings
import com.example.util.DateHelper
import java.time.LocalDate
import java.util.Calendar


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    strings: AppStrings,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.dayTasks.collectAsStateWithLifecycle()


    TimelineContent(
        uiState = uiState,
        tasks = tasks,
        strings = strings,
        onNavigateToCalendar = onNavigateToCalendar,
        onSelectDate = { viewModel.selectDate(it) },
        onOpenNewTaskSheet = { viewModel.openNewTaskSheet(it) },
        onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
        onCloseNewTaskSheet = { viewModel.closeNewTaskSheet() },
        onSaveTask = { viewModel.saveTask(it) },
        modifier = modifier
    )


}

// ۲. کل کدهای UI شما به این کامپوننت بدون وضعیت (Stateless) منتقل می‌شود
@Composable
fun TimelineContent(
    uiState: TimelineUiState,
    tasks: List<TaskItem>,
    strings: AppStrings,
    onNavigateToCalendar: () -> Unit,
    onSelectDate: (Long) -> Unit,
    onOpenNewTaskSheet: (TaskItem?) -> Unit,
    onToggleTaskComplete: (Long) -> Unit,
    onCloseNewTaskSheet: () -> Unit,
    onSaveTask: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateHeaderLabel = remember(uiState.selectedEpochDay, strings.isPersian) {
        if (strings.isPersian) {
            DateHelper.formatPersianHeaderDate(uiState.selectedEpochDay)
        } else {
            DateHelper.formatEnglishHeaderDate(uiState.selectedEpochDay)
        }
    }

    //تسک هاییی که در طول هفته نشان میدهد
    val anytimeTasks = remember(tasks) {
        tasks.filter { it.isAllDay || it.startTimeMinute < 0 }
    }


    //تسک هایی که دارای زمان هستند
    val timedTasks = remember(tasks) {
        tasks.filter { !it.isAllDay && it.startTimeMinute >= 0 }
            .sortedBy { it.startTimeMinute }
    }

    Scaffold(
        topBar = {
            TimelineHeader(
                dateLabel = dateHeaderLabel,
                strings = strings,
                onTodayClick = { onSelectDate(DateHelper.todayEpochDay()) },
                onMonthViewClick = onNavigateToCalendar
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenNewTaskSheet(null) },
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("timeline_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.newTask,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            WeeklyDateStrip(
                selectedEpochDay = uiState.selectedEpochDay,
                isPersian = strings.isPersian,
                onSelectDate = onSelectDate,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (anytimeTasks.isNotEmpty()) {
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
                                if (strings.isPersian) DateHelper.formatPersianNumber(
                                    anytimeTasks.size
                                ) else anytimeTasks.size
                            } ${strings.tasksCountSuffix}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            anytimeTasks.forEachIndexed { index, task ->
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
                                if (index < anytimeTasks.size - 1) {
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


            TimelineGrid(
                tasks = timedTasks,
                strings = strings,
                onTaskClick = { onOpenNewTaskSheet(it) },
                onToggleComplete = onToggleTaskComplete,
                selectedEpochDay = uiState.selectedEpochDay,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
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


// انتخاب روز از هفته
@Composable
private fun WeeklyDateStrip(
    selectedEpochDay: Long,
    isPersian: Boolean,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayEpoch = remember { DateHelper.todayEpochDay() }

    val days = remember(todayEpoch) {
        (-3..3).map { todayEpoch + it }
    }
    val dateHelper = DateHelper

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
                val cal = dateHelper.getCalendarForEpochDay(epochDay)
                val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                val shamsiDay = dateHelper.epochDayToPersianDirect(epochDay)
                val isSelected = epochDay == selectedEpochDay

                val dayLabel = if (isPersian) {
                    when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.SATURDAY -> "ش"
                        java.util.Calendar.SUNDAY -> "ی"
                        java.util.Calendar.MONDAY -> "د"
                        java.util.Calendar.TUESDAY -> "س"
                        java.util.Calendar.WEDNESDAY -> "چ"
                        java.util.Calendar.THURSDAY -> "پ"
                        else -> "ج"
                    }
                } else {
                    when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.MONDAY -> "M"
                        java.util.Calendar.TUESDAY -> "T"
                        java.util.Calendar.WEDNESDAY -> "W"
                        java.util.Calendar.THURSDAY -> "T"
                        java.util.Calendar.FRIDAY -> "F"
                        java.util.Calendar.SATURDAY -> "S"
                        else -> "S"
                    }
                }

                val dayNumStr =
                    if (isPersian) DateHelper.formatPersianNumber(shamsiDay.day) else dayOfMonth.toString()

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                        .clickable { onSelectDate(epochDay) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = dayNumStr,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}


// ۱. مدل داده تجمیع‌شده برای پاس دادن به Preview
data class TimelineGridPreviewData(
    val tasks: List<TaskItem>,
    val strings: AppStrings,
    val selectedEpochDay: Long
)

// ۲. ارائه‌دهنده مقادیر تستی با در نظر گرفتن ساختار تایم‌لاین (ساعت شروع، پایان و روز کامل)
class TimelineGridPreviewParameterProvider : PreviewParameterProvider<TimelineGridPreviewData> {
    @RequiresApi(Build.VERSION_CODES.O)
    private val todayEpoch = LocalDate.now().toEpochDay()

    @RequiresApi(Build.VERSION_CODES.O)
    override val values: Sequence<TimelineGridPreviewData> = sequenceOf(
        // حالت ۱: تایم‌لاین با تسک‌های زمان‌دار و روز کامل
        TimelineGridPreviewData(
            tasks = listOf(
                TaskItem(
                    id = 1L,
                    title = "بررسی ایمیل‌ها و پلن روزانه",
                    dateEpochDay = todayEpoch,
                    isAllDay = true,
                    isCompleted = true
                ),
                TaskItem(
                    id = 2L,
                    title = "جلسه اسکرام و بررسی اسپرینت",
                    description = "هماهنگی تسک‌های تیم فرانت‌اند و بک‌اند",
                    dateEpochDay = todayEpoch,
                    startTimeMinute = 9 * 60,       // 09:00
                    endTimeMinute = 10 * 60,        // 10:00
                    locationOrDetails = "اتاق جلسات آنلاین"
                ),
                TaskItem(
                    id = 3L,
                    title = "توسعه کامپوننت TimelineGrid",
                    description = "پیاده‌سازی Preview و اصلاح لاجیک درگ اند دراپ",
                    dateEpochDay = todayEpoch,
                    startTimeMinute = 11 * 60 + 30, // 11:30
                    endTimeMinute = 13 * 60,        // 13:00
                    isCompleted = false
                ),
                TaskItem(
                    id = 4L,
                    title = "بررسی کد و ادغام PRها",
                    dateEpochDay = todayEpoch,
                    startTimeMinute = 15 * 60,      // 15:00
                    endTimeMinute = 16 * 60 + 30    // 16:30
                )
            ),
            strings = AppStrings(AppLanguage.ENGLISH),
            selectedEpochDay = todayEpoch
        ),
        // حالت ۲: حالت خالی (Empty State)
        TimelineGridPreviewData(
            tasks = emptyList(),
            strings = AppStrings(AppLanguage.ENGLISH),
            selectedEpochDay = todayEpoch
        )
    )
}

// ۳. تابع پیش‌نمایش Composable
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


//تمامی ساعات
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
            // فیلتر تسک‌های متعلق به این ساعت
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
                        .padding(horizontal = 16.dp)
                ) {
                    // ۱. خط سرفصل: ساعت و Divider کاملاً در یک راستای افقی
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

                    // ۲. فضای محتوای تسک‌ها (زیر ساعت و خط)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 56.dp,
                                top = 6.dp,
                                bottom = 6.dp
                            ) // شروع دقیقاً هم‌راستا با Divider
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
                            // در صورت نبود تسک، ارتفاع حداقلی نگه داشته می‌شود
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

                // ۳. نشانگر زمان جاری (خط Now)
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
        // لیبل بیضی شکل نمایش زمان فعلی
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

        // دایره ابتدای خط
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // خط افقی
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
        // Completed Task Row
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
        // Active Task Card
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = androidx.compose.foundation.BorderStroke(
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
                    // Priority / Status Dot
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

                // Right trailing icon
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

package ir.arminniromandi.timekar.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.arminniromandi.timekar.domain.model.Priority
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.ui.components.NewTaskBottomSheet

import ir.arminniromandi.timekar.ui.strings.AppStrings
import ir.arminniromandi.timekar.util.DateHelper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDayTasks by viewModel.selectedDayTasks.collectAsStateWithLifecycle()

    val monthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthNamesFa = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    LaunchedEffect(strings.isPersian) {
        viewModel.goToToday(strings.isPersian)
    }

    val currentMonthTitle = remember(uiState.selectedMonth, uiState.selectedYear, strings.isPersian) {
        if (strings.isPersian) {
            val monthFa = monthNamesFa.getOrElse(uiState.selectedMonth - 1) { "" }
            val yearFa = DateHelper.formatPersianNumber(uiState.selectedYear)
            "$monthFa $yearFa"
        } else {
            val monthEn = monthNamesEn.getOrElse(uiState.selectedMonth - 1) { "" }
            "$monthEn ${uiState.selectedYear}"
        }
    }

    val selectedDateHeader = remember(uiState.selectedEpochDay, selectedDayTasks.size, strings.isPersian) {
        val dateStr = if (strings.isPersian) {
            DateHelper.formatPersianHeaderDate(uiState.selectedEpochDay)
        } else {
            DateHelper.formatEnglishHeaderDate(uiState.selectedEpochDay)
        }
        val countStr = if (strings.isPersian) {
            DateHelper.formatPersianNumber(selectedDayTasks.size)
        } else {
            selectedDayTasks.size.toString()
        }
        "$dateStr • $countStr ${strings.scheduledItemsSuffix}"
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Month Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentMonthTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // "Today" Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .clickable { viewModel.goToToday(strings.isPersian) }
                                    .testTag("calendar_today_button")
                            ) {
                                Text(
                                    text = strings.today,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Navigation arrows
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.previousMonth() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextMonth() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Grid Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MonthCalendarGrid(
                        year = uiState.selectedYear,
                        month = uiState.selectedMonth,
                        selectedEpochDay = uiState.selectedEpochDay,
                        allTasks = allTasks,
                        isPersian = strings.isPersian,
                        onSelectDay = { viewModel.selectDate(it, strings.isPersian) }
                    )
                }
            }

            // Agenda Header
            item {
                Text(
                    text = selectedDateHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Scheduled Tasks for Selected Day
            if (selectedDayTasks.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (strings.isPersian) "هیچ برنامه‌ای برای این روز ثبت نشده است" else "No scheduled events for this date",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {

                items(selectedDayTasks, key = { it.id }) { task ->
                    CalendarTaskCard(
                        task = task,
                        strings = strings,
                        onClick = { viewModel.openNewTaskSheet(task) },
                        onToggleComplete = { viewModel.toggleTaskComplete(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // New / Edit Task Bottom Sheet
        if (uiState.isNewTaskSheetVisible) {
            NewTaskBottomSheet(
                strings = strings,
                initialTask = uiState.taskToEdit,
                onDismiss = { viewModel.closeNewTaskSheet() },
                onSaveTask = { viewModel.saveTask(it) }
            )
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    year: Int,
    month: Int,
    selectedEpochDay: Long,
    allTasks: List<TaskItem>,
    isPersian: Boolean,
    onSelectDay: (Long) -> Unit
) {
    val headers = if (isPersian) {
        listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    } else {
        listOf("M", "T", "W", "T", "F", "S", "S")
    }

    // ۱. تعداد کل روزهای ماه
    val maxDays = if (isPersian) {
        DateHelper.getPersianMonthDays(year, month)
    } else {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // ۲. محاسبه روز اول ماه و خانه‌های خالی ابتدای گرید
    val leadingEmptyCells = if (isPersian) {
        val firstDayEpoch = DateHelper.persianToEpochDay(year, month, 1)
        val cal = DateHelper.getCalendarForEpochDay(firstDayEpoch)
        // در تقویم شمسی، شنبه شروع هفته است (Calendar.SATURDAY یعنی آفست ۰)
        (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    } else {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // در تقویم میلادی، دوشنبه شروع هفته است
        (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // سرستون روزهای هفته
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            headers.forEach { h ->
                Text(
                    text = h,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // خانه‌های روزهای ماه
        val totalCells = leadingEmptyCells + maxDays
        val rows = (totalCells + 6) / 7

        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNum = cellIndex - leadingEmptyCells + 1

                    if (dayNum in 1..maxDays) {
                        // تولید EpochDay دقیق روز جاری
                        val epochDay = if (isPersian) {
                            DateHelper.persianToEpochDay(year, month, dayNum)
                        } else {
                            DateHelper.epochDayFromDate(year, month, dayNum)
                        }

                        val isSelected = epochDay == selectedEpochDay
                        val dayTasks = allTasks.filter { it.dateEpochDay == epochDay }
                        val hasHighPriority = dayTasks.any { it.priority == Priority.HIGH }
                        val hasMediumPriority = dayTasks.any { it.priority == Priority.MEDIUM }
                        val dayNumStr = if (isPersian) DateHelper.formatPersianNumber(dayNum) else dayNum.toString()

                        Column(
                            modifier = Modifier
                                .width(38.dp)
                                .height(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                                .clickable { onSelectDay(epochDay) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNumStr,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.onSurface
                            )

                            // نشانگر وجود تسک در روز
                            if (dayTasks.isNotEmpty()) {
                                val dotColor = when {
                                    isSelected -> MaterialTheme.colorScheme.surfaceContainerLowest
                                    hasHighPriority -> Color(Priority.HIGH.colorHex)
                                    hasMediumPriority -> Color(Priority.MEDIUM.colorHex)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarTaskCard(
    task: TaskItem,
    strings: AppStrings,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        1.5.dp,
                        if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
                    .clickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = task.formattedTimeSlot,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            text = if (strings.isPersian) task.category.persianLabel else task.category.englishLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.subtasks.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                            val completedCount = task.completedSubtasksCount
                            val countText = if (strings.isPersian) {
                                "${DateHelper.formatPersianNumber(completedCount)}/${DateHelper.formatPersianNumber(task.subtasks.size)}"
                            } else {
                                "$completedCount/${task.subtasks.size}"
                            }
                            Text(
                                text = countText,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

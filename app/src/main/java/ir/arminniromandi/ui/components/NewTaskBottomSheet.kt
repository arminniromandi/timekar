package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Category
import com.example.domain.model.Priority
import com.example.domain.model.SubtaskItem
import com.example.domain.model.TaskItem
import com.example.ui.strings.AppStrings
import com.example.util.DateHelper
import ir.arminniromandi.ui.components.TimePickerDialog
import java.util.Calendar

private enum class ActivePicker {
    NONE, DATE, TIME, PRIORITY, CATEGORY
}

private enum class TimePickerTarget {
    START, END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskBottomSheet(
    strings: AppStrings,
    initialTask: TaskItem? = null,
    onDismiss: () -> Unit,
    onSaveTask: (TaskItem) -> Unit
) {
    val currentTime = Calendar.getInstance()
    val nowTime = currentTime.get(Calendar.HOUR_OF_DAY)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val todayEpoch = remember { DateHelper.todayEpochDay() }

    // محاسبه دقیق روزهای پایان هفته و هفته بعد بر اساس زبان و تقویم
    val (weekendEpoch, nextWeekEpoch) = remember(todayEpoch, strings.isPersian) {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 6 = Friday, 7 = Saturday

        val daysUntilWeekend = if (strings.isPersian) {
            // پایان هفته برای تقویم شمسی (جمعه)
            val diff = Calendar.FRIDAY - dayOfWeek
            if (diff <= 0) diff + 7 else diff
        } else {
            // پایان هفته برای تقویم میلادی (یکشنبه)
            val diff = Calendar.SUNDAY - dayOfWeek
            if (diff <= 0) diff + 7 else diff
        }

        val daysUntilNextWeek = if (strings.isPersian) {
            // شنبه اول هفته آینده
            val diff = (Calendar.SATURDAY - dayOfWeek + 7) % 7
            if (diff == 0) 7 else diff
        } else {
            // دوشنبه اول هفته آینده میلادی
            val diff = (Calendar.MONDAY - dayOfWeek + 7) % 7
            if (diff == 0) 7 else diff
        }

        Pair(todayEpoch + daysUntilWeekend, todayEpoch + daysUntilNextWeek)
    }

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var selectedEpochDay by remember { mutableLongStateOf(initialTask?.dateEpochDay ?: todayEpoch) }
    var startMinute by remember {
        mutableIntStateOf(
            initialTask?.startTimeMinute ?: (nowTime * 60)
        )
    }
    var endMinute by remember {
        mutableIntStateOf(
            initialTask?.endTimeMinute ?: ((nowTime + 1) * 60)
        )
    }
    var isAllDay by remember { mutableStateOf(initialTask?.isAllDay ?: false) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.HIGH) }
    var category by remember { mutableStateOf(initialTask?.category ?: Category.PRODUCT_CORE) }
    var reminderText by remember {
        mutableStateOf(
            initialTask?.reminderText
                ?: if (strings.isPersian) "۱۰ دقیقه قبل از طریق بنر سیستم" else "10 minutes prior via system banner"
        )
    }
    var recurrenceText by remember {
        mutableStateOf(
            initialTask?.recurrenceText
                ?: if (strings.isPersian) "هفتگی در روزهای پنج‌شنبه" else "Weekly on Thursday"
        )
    }

    val subtasks = remember {
        mutableStateListOf<SubtaskItem>().apply {
            if (initialTask != null && initialTask.subtasks.isNotEmpty()) {
                addAll(initialTask.subtasks)
            }
        }
    }

    var newSubtaskInput by remember { mutableStateOf("") }
    var isAddingSubtask by remember { mutableStateOf(false) }

    var activePicker by remember { mutableStateOf(ActivePicker.NONE) }

    var timePickerTarget by remember {
        mutableStateOf<TimePickerTarget?>(null)
    }

    var timePickerState by remember {
        mutableStateOf<TimePickerState?>(null)
    }

    if (timePickerTarget != null) {
        TimePickerDialog(
            onDismiss = {
                timePickerTarget = null
                timePickerState = null
            },
            onConfirm = {
                when (timePickerTarget) {
                    TimePickerTarget.START -> {
                        startMinute = it
                    }

                    TimePickerTarget.END -> {
                        endMinute = it
                    }

                    null -> Unit
                }

                timePickerTarget = null
                timePickerState = null
            },
            timePickerState = timePickerState!!
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.cancel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("cancel_task_button")
                )

                Text(
                    text = if (initialTask == null) strings.newTask else strings.editTask,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newTaskItem = TaskItem(
                                id = initialTask?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                dateEpochDay = selectedEpochDay,
                                startTimeMinute = if (isAllDay) -1 else startMinute,
                                endTimeMinute = if (isAllDay) -1 else endMinute,
                                isAllDay = isAllDay,
                                priority = priority,
                                category = category,
                                locationOrDetails = initialTask?.locationOrDetails ?: "",
                                reminderText = reminderText,
                                recurrenceText = recurrenceText,
                                isCompleted = initialTask?.isCompleted ?: false,
                                subtasks = subtasks.toList()
                            )
                            onSaveTask(newTaskItem)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_task_button")
                ) {
                    Text(
                        text = strings.save,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Input
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = strings.taskTitlePlaceholder,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Description Input
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = strings.taskDescPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                // Quick Attribute Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Date Chip
                    item {
                        val formattedDate = if (strings.isPersian) {
                            DateHelper.formatPersianHeaderDate(selectedEpochDay)
                        } else {
                            DateHelper.formatEnglishHeaderDate(selectedEpochDay)
                        }

                        val dateLabel = when (selectedEpochDay) {
                            todayEpoch -> "${strings.presetToday}، $formattedDate"
                            todayEpoch + 1 -> "${strings.presetTomorrow}، $formattedDate"
                            else -> formattedDate
                        }

                        AttributeChip(
                            icon = Icons.Default.CalendarToday,
                            label = dateLabel,
                            isActive = activePicker == ActivePicker.DATE,
                            onClick = {
                                activePicker =
                                    if (activePicker == ActivePicker.DATE) ActivePicker.NONE else ActivePicker.DATE
                            },
                            testTag = "chip_date"
                        )
                    }

                    // Time Chip
                    item {
                        val timeLabel = if (isAllDay) {
                            strings.allDayEvent
                        } else {
                            val startH = String.format("%02d", startMinute / 60)
                            val startM = String.format("%02d", startMinute % 60)
                            val endH = String.format("%02d", endMinute / 60)
                            val endM = String.format("%02d", endMinute % 60)
                            if (strings.isPersian) {
                                "${DateHelper.formatPersianNumber(startH.toInt())}:${
                                    DateHelper.formatPersianNumber(
                                        startM.toInt()
                                    )
                                } – ${DateHelper.formatPersianNumber(endH.toInt())}:${
                                    DateHelper.formatPersianNumber(
                                        endM.toInt()
                                    )
                                }"
                            } else {
                                "$startH:$startM – $endH:$endM"
                            }
                        }
                        AttributeChip(
                            icon = Icons.Default.Schedule,
                            label = timeLabel,
                            isActive = activePicker == ActivePicker.TIME,
                            onClick = {
                                activePicker =
                                    if (activePicker == ActivePicker.TIME) ActivePicker.NONE else ActivePicker.TIME
                            },
                            testTag = "chip_time"
                        )
                    }

                    // Priority Chip
                    item {
                        val priorityColor = Color(priority.colorHex)
                        val priorityLabel = when (priority) {
                            Priority.HIGH -> if (strings.isPersian) "اولویت بالا" else "High Priority"
                            Priority.MEDIUM -> if (strings.isPersian) "اولویت متوسط" else "Medium Priority"
                            Priority.NORMAL -> if (strings.isPersian) "اولویت عادی" else "Normal Priority"
                            Priority.LOW -> if (strings.isPersian) "اولویت پایین" else "Low Priority"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activePicker == ActivePicker.PRIORITY) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.2f
                            ) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activePicker == ActivePicker.PRIORITY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            modifier = Modifier
                                .clickable {
                                    activePicker =
                                        if (activePicker == ActivePicker.PRIORITY) ActivePicker.NONE else ActivePicker.PRIORITY
                                }
                                .testTag("chip_priority")
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(priorityColor)
                                )
                                Text(
                                    text = priorityLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Category Chip
                    item {
                        val categoryLabel =
                            if (strings.isPersian) category.persianLabel else category.englishLabel
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activePicker == ActivePicker.CATEGORY) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.2f
                            ) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activePicker == ActivePicker.CATEGORY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            modifier = Modifier
                                .clickable {
                                    activePicker =
                                        if (activePicker == ActivePicker.CATEGORY) ActivePicker.NONE else ActivePicker.CATEGORY
                                }
                                .testTag("chip_category")
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "#",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = categoryLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // ================= PICKER PANELS =================

                // 1. DATE PICKER PANEL
                AnimatedVisibility(
                    visible = activePicker == ActivePicker.DATE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.selectDueDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { activePicker = ActivePicker.NONE },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // 4 Date Presets محاسبه‌شده و دقیق
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DatePresetButton(
                                    title = strings.presetToday,
                                    isSelected = selectedEpochDay == todayEpoch,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedEpochDay = todayEpoch
                                }
                                DatePresetButton(
                                    title = strings.presetTomorrow,
                                    isSelected = selectedEpochDay == todayEpoch + 1,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedEpochDay = todayEpoch + 1
                                }
                                DatePresetButton(
                                    title = strings.presetWeekend,
                                    isSelected = selectedEpochDay == weekendEpoch,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedEpochDay = weekendEpoch
                                }
                                DatePresetButton(
                                    title = strings.presetNextWeek,
                                    isSelected = selectedEpochDay == nextWeekEpoch,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedEpochDay = nextWeekEpoch
                                }
                            }
                        }
                    }
                }

                // 2. TIME PICKER PANEL
                AnimatedVisibility(
                    visible = activePicker == ActivePicker.TIME,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.scheduleTimeAndDuration,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { activePicker = ActivePicker.NONE },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // All-day switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.allDayEvent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = isAllDay,
                                    onCheckedChange = { isAllDay = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            if (!isAllDay) {
                                // Start / End Time fields
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TimeDisplayBox(
                                        label = strings.startTime,
                                        minute = startMinute,
                                        isPersian = strings.isPersian,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                timePickerTarget = TimePickerTarget.START
                                                timePickerState = TimePickerState(
                                                    initialHour = startMinute / 60,
                                                    initialMinute = startMinute % 60,
                                                    is24Hour = true
                                                )
                                            }
                                    )
                                    TimeDisplayBox(
                                        label = strings.endTime,
                                        minute = endMinute,
                                        isPersian = strings.isPersian,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                timePickerTarget = TimePickerTarget.END
                                                timePickerState = TimePickerState(
                                                    initialHour = endMinute / 60,
                                                    initialMinute = endMinute % 60,
                                                    is24Hour = true
                                                )
                                            }
                                    )
                                }

                                // Quick Durations
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = strings.quickDuration,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        DurationChip(
                                            title = if (strings.isPersian) "۱۵ د" else "15m",
                                            isSelected = (endMinute - startMinute) == 15,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            endMinute = startMinute + 15
                                        }
                                        DurationChip(
                                            title = if (strings.isPersian) "۳۰ د" else "30m",
                                            isSelected = (endMinute - startMinute) == 30,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            endMinute = startMinute + 30
                                        }
                                        DurationChip(
                                            title = if (strings.isPersian) "۴۵ د" else "45m",
                                            isSelected = (endMinute - startMinute) == 45,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            endMinute = startMinute + 45
                                        }
                                        DurationChip(
                                            title = if (strings.isPersian) "۱ س" else "1h",
                                            isSelected = (endMinute - startMinute) == 60,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            endMinute = startMinute + 60
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. PRIORITY PICKER PANEL
                AnimatedVisibility(
                    visible = activePicker == ActivePicker.PRIORITY,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.selectPriority,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { activePicker = ActivePicker.NONE },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PriorityOptionCard(
                                    title = strings.p1Title,
                                    subtitle = strings.p1Sub,
                                    color = Color(Priority.HIGH.colorHex),
                                    isSelected = priority == Priority.HIGH,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    priority = Priority.HIGH
                                    activePicker = ActivePicker.NONE
                                }
                                PriorityOptionCard(
                                    title = strings.p2Title,
                                    subtitle = strings.p2Sub,
                                    color = Color(Priority.MEDIUM.colorHex),
                                    isSelected = priority == Priority.MEDIUM,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    priority = Priority.MEDIUM
                                    activePicker = ActivePicker.NONE
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PriorityOptionCard(
                                    title = strings.p3Title,
                                    subtitle = strings.p3Sub,
                                    color = Color(Priority.NORMAL.colorHex),
                                    isSelected = priority == Priority.NORMAL,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    priority = Priority.NORMAL
                                    activePicker = ActivePicker.NONE
                                }
                                PriorityOptionCard(
                                    title = strings.p4Title,
                                    subtitle = strings.p4Sub,
                                    color = Color(Priority.LOW.colorHex),
                                    isSelected = priority == Priority.LOW,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    priority = Priority.LOW
                                    activePicker = ActivePicker.NONE
                                }
                            }
                        }
                    }
                }

                // 4. CATEGORY PICKER PANEL
                AnimatedVisibility(
                    visible = activePicker == ActivePicker.CATEGORY,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (strings.isPersian) "انتخاب دسته‌بندی" else "Select Category",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(Category.entries) { cat ->
                                    val isSelected = category == cat
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                                        ),
                                        modifier = Modifier.clickable {
                                            category = cat
                                            activePicker = ActivePicker.NONE
                                        }
                                    ) {
                                        Text(
                                            text = if (strings.isPersian) cat.persianLabel else cat.englishLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    thickness = 0.8.dp
                )

                // Progressive Disclosure Section: Contextual Metadata
                // 1. Reminder
                val reminderOptions = remember {
                    if (strings.isPersian) {
                        listOf(
                            "۱۰ دقیقه قبل از طریق بنر سیستم",
                            "۳۰ دقیقه قبل",
                            "۱ ساعت قبل",
                            "در زمان رویداد",
                            "هیچ"
                        )
                    } else {
                        listOf(
                            "10 minutes prior via system banner",
                            "30 minutes prior",
                            "1 hour prior",
                            "At time of event",
                            "None"
                        )
                    }
                }
                var remIndex by remember { mutableIntStateOf(0) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = strings.reminder,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = strings.reminder,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = reminderText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Text(
                        text = strings.change,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                remIndex = (remIndex + 1) % reminderOptions.size
                                reminderText = reminderOptions[remIndex]
                            }
                            .padding(4.dp)
                    )
                }

                // 2. Recurrence
                val recurrenceOptions = remember {
                    if (strings.isPersian) {
                        listOf(
                            "هفتگی در روزهای پنج‌شنبه",
                            "روزانه",
                            "هر روز کاری (شنبه تا چهارشنبه)",
                            "ماهانه",
                            "تکرار نمی‌شود"
                        )
                    } else {
                        listOf(
                            "Weekly on Thursday",
                            "Daily",
                            "Every weekday (Mon-Fri)",
                            "Monthly",
                            "Does not repeat"
                        )
                    }
                }
                var recIndex by remember { mutableIntStateOf(0) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = strings.recurrence,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = strings.recurrence,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = recurrenceText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Text(
                        text = strings.edit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                recIndex = (recIndex + 1) % recurrenceOptions.size
                                recurrenceText = recurrenceOptions[recIndex]
                            }
                            .padding(4.dp)
                    )
                }

                // 3. Subtasks Checklist
                val completedSubtasks = subtasks.count { it.isCompleted }
                val subtaskCountStr = if (strings.isPersian) {
                    " (${DateHelper.formatPersianNumber(completedSubtasks)}/${
                        DateHelper.formatPersianNumber(
                            subtasks.size
                        )
                    })"
                } else {
                    " ($completedSubtasks/${subtasks.size})"
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = strings.subtasks,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.subtasks + subtaskCountStr,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Subtask items
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subtasks.forEachIndexed { index, subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        subtasks[index] =
                                            subtask.copy(isCompleted = !subtask.isCompleted)
                                    }
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (subtask.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .border(
                                                1.5.dp,
                                                MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                    )
                                }

                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Add Subtask Box
                        if (isAddingSubtask) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = newSubtaskInput,
                                    onValueChange = { newSubtaskInput = it },
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    decorationBox = { inner ->
                                        if (newSubtaskInput.isEmpty()) {
                                            Text(
                                                text = if (strings.isPersian) "عنوان زیرکار را بنویسید..." else "Enter subtask title...",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerLow,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                                Text(
                                    text = strings.save,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            if (newSubtaskInput.isNotBlank()) {
                                                subtasks.add(
                                                    SubtaskItem(
                                                        title = newSubtaskInput.trim(),
                                                        isCompleted = false
                                                    )
                                                )
                                                newSubtaskInput = ""
                                                isAddingSubtask = false
                                            }
                                        }
                                        .padding(4.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .clickable { isAddingSubtask = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = strings.addItem,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.addItem,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Toolbar (Material 3 Mobile Utility Bar)
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.8.dp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = { activePicker = ActivePicker.DATE },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { activePicker = ActivePicker.TIME },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { activePicker = ActivePicker.PRIORITY },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Priority",
                                tint = Color(priority.colorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isAddingSubtask = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Subtasks",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { activePicker = ActivePicker.CATEGORY },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = "Category",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Floating confirm circle button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                if (title.isNotBlank()) {
                                    val newTaskItem = TaskItem(
                                        id = initialTask?.id ?: 0,
                                        title = title.trim(),
                                        description = description.trim(),
                                        dateEpochDay = selectedEpochDay,
                                        startTimeMinute = if (isAllDay) -1 else startMinute,
                                        endTimeMinute = if (isAllDay) -1 else endMinute,
                                        isAllDay = isAllDay,
                                        priority = priority,
                                        category = category,
                                        locationOrDetails = initialTask?.locationOrDetails ?: "",
                                        reminderText = reminderText,
                                        recurrenceText = recurrenceText,
                                        isCompleted = initialTask?.isCompleted ?: false,
                                        subtasks = subtasks.toList()
                                    )
                                    onSaveTask(newTaskItem)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Confirm",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttributeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun DatePresetButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DurationChip(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TimeDisplayBox(
    label: String,
    minute: Int,
    isPersian: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = DateHelper.formatMinuteTime(minute, isPersian),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PriorityOptionCard(
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
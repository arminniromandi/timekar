package ir.arminniromandi.timekar.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.ui.components.NewTaskBottomSheet

import ir.arminniromandi.timekar.ui.strings.AppStrings
import ir.arminniromandi.timekar.util.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()

    val todayEpoch = remember { DateHelper.todayEpochDay() }

    val overdueTasks = remember(filteredTasks, todayEpoch) {
        filteredTasks.filter { it.dateEpochDay < todayEpoch && !it.isCompleted }
    }
    val todayTasks = remember(filteredTasks, todayEpoch) {
        filteredTasks.filter { it.dateEpochDay == todayEpoch && !it.isCompleted }
    }
    val upcomingTasks = remember(filteredTasks, todayEpoch) {
        filteredTasks.filter { it.dateEpochDay > todayEpoch && !it.isCompleted }
    }
    val completedTasks = remember(filteredTasks) {
        filteredTasks.filter { it.isCompleted }
    }

    // Counts for filter pills
    val allCount = allTasks.size
    val todayCount = allTasks.count { it.dateEpochDay == todayEpoch }
    val upcomingCount = allTasks.count { it.dateEpochDay > todayEpoch && !it.isCompleted }
    val overdueCount = allTasks.count { it.dateEpochDay < todayEpoch && !it.isCompleted }
    val completedCount = allTasks.count { it.isCompleted }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.tasks,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleSearch() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("tasks_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = strings.search,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.setFilter(TaskFilter.ALL) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("tasks_tune_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = strings.filter,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Search Field (if expanded)
                    if (uiState.isSearchActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = strings.search + "...",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Filter Chips Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChipItem(
                                label = strings.filterAll,
                                count = allCount,
                                isSelected = uiState.selectedFilter == TaskFilter.ALL,
                                isPersian = strings.isPersian,
                                onClick = { viewModel.setFilter(TaskFilter.ALL) },
                                testTag = "filter_all"
                            )
                        }
                        item {
                            FilterChipItem(
                                label = strings.filterToday,
                                count = todayCount,
                                isSelected = uiState.selectedFilter == TaskFilter.TODAY,
                                isPersian = strings.isPersian,
                                onClick = { viewModel.setFilter(TaskFilter.TODAY) },
                                testTag = "filter_today"
                            )
                        }
                        item {
                            FilterChipItem(
                                label = strings.filterUpcoming,
                                count = upcomingCount,
                                isSelected = uiState.selectedFilter == TaskFilter.UPCOMING,
                                isPersian = strings.isPersian,
                                onClick = { viewModel.setFilter(TaskFilter.UPCOMING) },
                                testTag = "filter_upcoming"
                            )
                        }
                        item {
                            FilterChipItem(
                                label = strings.filterOverdue,
                                count = overdueCount,
                                isSelected = uiState.selectedFilter == TaskFilter.OVERDUE,
                                isPersian = strings.isPersian,
                                isAlert = overdueCount > 0,
                                onClick = { viewModel.setFilter(TaskFilter.OVERDUE) },
                                testTag = "filter_overdue"
                            )
                        }
                        item {
                            FilterChipItem(
                                label = strings.filterCompleted,
                                count = completedCount,
                                isSelected = uiState.selectedFilter == TaskFilter.COMPLETED,
                                isPersian = strings.isPersian,
                                onClick = { viewModel.setFilter(TaskFilter.COMPLETED) },
                                testTag = "filter_completed"
                            )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. OVERDUE SECTION
            if (overdueTasks.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${if (strings.isPersian) DateHelper.formatPersianNumber(overdueTasks.size) else overdueTasks.size} ${strings.overdueSection}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                letterSpacing = 0.5.sp
                            )
                        }

                        overdueTasks.forEach { task ->
                            TasksListCard(
                                task = task,
                                strings = strings,
                                isOverdue = true,
                                onClick = { viewModel.openNewTaskSheet(task) },
                                onToggleComplete = { viewModel.toggleTaskComplete(task.id) }
                            )
                        }
                    }
                }
            }

            // 2. TODAY SECTION
            if (todayTasks.isNotEmpty() || (uiState.selectedFilter == TaskFilter.TODAY && filteredTasks.isEmpty())) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.todaySection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (strings.isPersian) DateHelper.formatPersianNumber(todayTasks.size) else todayTasks.size} ${strings.tasksCountSuffix}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        todayTasks.forEach { task ->
                            TasksListCard(
                                task = task,
                                strings = strings,
                                isOverdue = false,
                                onClick = { viewModel.openNewTaskSheet(task) },
                                onToggleComplete = { viewModel.toggleTaskComplete(task.id) }
                            )
                        }
                    }
                }
            }

            // 3. UPCOMING SECTION
            if (upcomingTasks.isNotEmpty() || uiState.selectedFilter == TaskFilter.UPCOMING) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.upcomingSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.tomorrowAndBeyond,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        upcomingTasks.forEach { task ->
                            TasksListCard(
                                task = task,
                                strings = strings,
                                isOverdue = false,
                                onClick = { viewModel.openNewTaskSheet(task) },
                                onToggleComplete = { viewModel.toggleTaskComplete(task.id) }
                            )
                        }
                    }
                }
            }

            // 4. COMPLETED SECTION
            if (completedTasks.isNotEmpty() || uiState.selectedFilter == TaskFilter.COMPLETED) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleCompletedSection() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.completedSection} (${if (strings.isPersian) DateHelper.formatPersianNumber(completedTasks.size) else completedTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (uiState.isCompletedSectionExpanded) strings.hide else strings.show,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (uiState.isCompletedSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = uiState.isCompletedSectionExpanded || uiState.selectedFilter == TaskFilter.COMPLETED) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                completedTasks.forEach { task ->
                                    TasksListCard(
                                        task = task,
                                        strings = strings,
                                        isOverdue = false,
                                        onClick = { viewModel.openNewTaskSheet(task) },
                                        onToggleComplete = { viewModel.toggleTaskComplete(task.id) }
                                    )
                                }
                            }
                        }
                    }
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
                onSaveTask = { viewModel.saveTask(it ) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    isPersian: Boolean,
    isAlert: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val countStr = if (isPersian) DateHelper.formatPersianNumber(count) else count.toString()
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        isAlert -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.surfaceContainerLowest
        isAlert -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            Text(
                text = countStr,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TasksListCard(
    task: TaskItem,
    strings: AppStrings,
    isOverdue: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val todayEpoch = remember { DateHelper.todayEpochDay() }
    val borderColor = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHighest

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
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

            // Task info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                if (task.description.isNotBlank() && !task.isCompleted) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 2
                    )
                }

                // Attributes row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Category
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            text = if (strings.isPersian) task.category.persianLabel else task.category.englishLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Date / Time
                    val dateLabel = when {
                        task.dateEpochDay == todayEpoch -> task.formattedTimeSlot
                        task.dateEpochDay == todayEpoch - 1 -> if (strings.isPersian) "دیروز" else "Yesterday"
                        task.dateEpochDay == todayEpoch + 1 -> if (strings.isPersian) "فردا" else "Tomorrow"
                        else -> if (strings.isPersian) DateHelper.formatPersianHeaderDate(task.dateEpochDay) else DateHelper.formatEnglishHeaderDate(task.dateEpochDay)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = dateLabel,
                            fontSize = 11.sp,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Subtasks counter if any
                    if (task.subtasks.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(13.dp)
                            )
                            val completedCount = task.completedSubtasksCount
                            val countText = if (strings.isPersian) {
                                "${DateHelper.formatPersianNumber(completedCount)}/${DateHelper.formatPersianNumber(task.subtasks.size)}"
                            } else {
                                "$completedCount/${task.subtasks.size}"
                            }
                            Text(
                                text = countText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

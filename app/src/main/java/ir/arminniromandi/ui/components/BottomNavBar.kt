package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.strings.AppStrings

enum class NavTab {
    TIMELINE,
    TASKS,
    CALENDAR,
    SETTINGS
}




@Composable
    fun ChronosBottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {


    val navBarItemColor = NavigationBarItemDefaults.colors(
        // ۱. رنگ کپسول بیضی‌شکل پشت آیکون انتخاب‌شده:
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,

        // ۲. رنگ آیکون انتخاب‌شده درون کپسول:
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,

        // ۳. رنگ متن تب انتخاب‌شده (می‌تواند خود primary باشد):
        selectedTextColor = MaterialTheme.colorScheme.primary,

        // رنگ تب‌های غیرفعال:
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        modifier = modifier
    ) {
        NavigationBarItem(

            selected = currentTab == NavTab.TIMELINE,
            onClick = { onTabSelected(NavTab.TIMELINE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavTab.TIMELINE) Icons.Filled.Schedule else Icons.Outlined.Schedule,
                    contentDescription = strings.timeline
                )
            },
            label = {
                Text(text = strings.timeline)
            },
            colors = navBarItemColor,
            modifier = Modifier.testTag("nav_timeline_tab")
        )

        NavigationBarItem(
            selected = currentTab == NavTab.TASKS,
            onClick = { onTabSelected(NavTab.TASKS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavTab.TASKS) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = strings.tasks
                )
            },
            label = {
                Text(text = strings.tasks)
            },
            colors = navBarItemColor,
            modifier = Modifier.testTag("nav_tasks_tab")
        )

        NavigationBarItem(
            selected = currentTab == NavTab.CALENDAR,
            onClick = { onTabSelected(NavTab.CALENDAR) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = strings.calendar
                )
            },
            label = {
                Text(text = strings.calendar)
            },
            colors = navBarItemColor,
            modifier = Modifier.testTag("nav_calendar_tab")
        )

        NavigationBarItem(
            selected = currentTab == NavTab.SETTINGS,
            onClick = { onTabSelected(NavTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == NavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = strings.settings
                )
            },
            label = {
                Text(text = strings.settings)
            },
            colors = navBarItemColor,
            modifier = Modifier.testTag("nav_settings_tab")
        )
    }
}

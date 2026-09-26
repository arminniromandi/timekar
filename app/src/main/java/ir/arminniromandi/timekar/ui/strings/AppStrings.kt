package ir.arminniromandi.timekar.ui.strings

import ir.arminniromandi.timekar.domain.AppLanguage


class AppStrings(val language: AppLanguage) {
    val isPersian: Boolean get() = language == AppLanguage.PERSIAN

    // Navigation & General
    val appTitle: String get() = if (isPersian) "کرونوس" else "Chronos"
    val timeline: String get() = if (isPersian) "خط زمانی" else "Timeline"
    val tasks: String get() = if (isPersian) "وظایف" else "Tasks"
    val calendar: String get() = if (isPersian) "تقویم" else "Calendar"
    val settings: String get() = if (isPersian) "تنظیمات" else "Settings"

    // Top Bar Actions
    val search: String get() = if (isPersian) "جستجو" else "Search"
    val filter: String get() = if (isPersian) "فیلتر" else "Filter"
    val today: String get() = if (isPersian) "امروز" else "Today"
    val viewSwitcher: String get() = if (isPersian) "تغییر نما" else "View Switcher"
    val monthView: String get() = if (isPersian) "نمای ماهانه" else "Month View"

    // Tasks Screen Filter Chips
    val filterAll: String get() = if (isPersian) "همه" else "All"
    val filterToday: String get() = if (isPersian) "امروز" else "Today"
    val filterUpcoming: String get() = if (isPersian) "آینده" else "Upcoming"
    val filterOverdue: String get() = if (isPersian) "معوق" else "Overdue"
    val filterCompleted: String get() = if (isPersian) "تکمیل‌شده" else "Completed"

    // Sections
    val overdueSection: String get() = if (isPersian) "معوق" else "OVERDUE"
    val todaySection: String get() = if (isPersian) "امروز" else "Today"
    val upcomingSection: String get() = if (isPersian) "آینده" else "Upcoming"
    val tomorrowAndBeyond: String get() = if (isPersian) "فردا و بعد" else "Tomorrow & beyond"
    val completedSection: String get() = if (isPersian) "تکمیل‌شده" else "Completed"
    val show: String get() = if (isPersian) "نمایش" else "Show"
    val hide: String get() = if (isPersian) "بستن" else "Hide"
    val anytime: String get() = if (isPersian) "در طول روز" else "ANYTIME"
    val tasksCountSuffix: String get() = if (isPersian) "کار" else "tasks"
    val scheduledItemsSuffix: String get() = if (isPersian) "مورد برنامه‌ریزی‌شده" else "scheduled items"

    // Modal Sheet
    val newTask: String get() = if (isPersian) "کار جدید" else "New Task"
    val editTask: String get() = if (isPersian) "ویرایش کار" else "Edit Task"
    val cancel: String get() = if (isPersian) "انصراف" else "Cancel"
    val save: String get() = if (isPersian) "ذخیره" else "Save"
    val taskTitlePlaceholder: String get() = if (isPersian) "چه کاری باید انجام شود؟" else "What needs to be done?"
    val taskDescPlaceholder: String get() = if (isPersian) "افزودن یادداشت یا توضیحات..." else "Add notes or description..."
    val selectDueDate: String get() = if (isPersian) "انتخاب تاریخ موعد" else "Select Due Date"
    val scheduleTimeAndDuration: String get() = if (isPersian) "تنظیم ساعت و مدت زمان" else "Schedule Time & Duration"
    val selectPriority: String get() = if (isPersian) "انتخاب اولویت" else "Select Priority"
    val allDayEvent: String get() = if (isPersian) "تمام روز" else "All-day Event"
    val startTime: String get() = if (isPersian) "ساعت شروع" else "Start Time"
    val endTime: String get() = if (isPersian) "ساعت پایان" else "End Time"
    val quickDuration: String get() = if (isPersian) "مدت زمان سریع" else "Quick Duration"
    val reminder: String get() = if (isPersian) "یادآور" else "Reminder"
    val recurrence: String get() = if (isPersian) "تکرار" else "Recurrence"
    val subtasks: String get() = if (isPersian) "زیرکارها" else "Subtasks"
    val addItem: String get() = if (isPersian) "افزودن مورد" else "Add item"
    val change: String get() = if (isPersian) "تغییر" else "Change"
    val edit: String get() = if (isPersian) "ویرایش" else "Edit"
    val done: String get() = if (isPersian) "انجام شد" else "Done"

    // Presets
    val presetToday: String get() = if (isPersian) "امروز" else "Today"
    val presetTomorrow: String get() = if (isPersian) "فردا" else "Tomorrow"
    val presetWeekend: String get() = if (isPersian) "آخر هفته" else "Weekend"
    val presetNextWeek: String get() = if (isPersian) "هفته آینده" else "Next Week"

    // Priority labels
    val p1Title: String get() = if (isPersian) "P1 • فوری / بالا" else "P1 • Urgent / High"
    val p1Sub: String get() = if (isPersian) "نیازمند رسیدگی سریع" else "Immediate attention"
    val p2Title: String get() = if (isPersian) "P2 • متوسط" else "P2 • Medium"
    val p2Sub: String get() = if (isPersian) "روند کاری استاندارد" else "Standard workflow"
    val p3Title: String get() = if (isPersian) "P3 • عادی" else "P3 • Normal"
    val p3Sub: String get() = if (isPersian) "در زمان مناسب" else "When time permits"
    val p4Title: String get() = if (isPersian) "P4 • پایین / هیچ" else "P4 • Low / None"
    val p4Sub: String get() = if (isPersian) "فهرست انتظار" else "Backlog item"

    //error Message
    val timeErrorMessage: String get() = if (isPersian) "زمان پایان نمی تواند قبل از زمان شروع باشد" else "End time cannot be earlier than start time."
    val titleIsEmptyError : String get() = if (isPersian) "عنوان نمیتواند خالی باشد!" else "Title cannot be empty."

    // Settings Screen
    val general: String get() = if (isPersian) "عمومی" else "GENERAL"
    val appLanguage: String get() = if (isPersian) "زبان برنامه" else "App Language"
    val defaultStartDay: String get() = if (isPersian) "روز شروع پیش‌فرض" else "Default Start Day"
    val appearance: String get() = if (isPersian) "ظاهر و پوسته" else "APPEARANCE"
    val themeMode: String get() = if (isPersian) "حالت تم" else "Theme Mode"
    val themeLight: String get() = if (isPersian) "روشن" else "Light"
    val themeDark: String get() = if (isPersian) "تاریک" else "Dark"
    val themeSystem: String get() = if (isPersian) "سیستم" else "System"
    val lightActive: String get() = if (isPersian) "روشن فعال" else "Light Active"
    val darkActive: String get() = if (isPersian) "تاریک فعال" else "Dark Active"
    val systemActive: String get() = if (isPersian) "سیستم فعال" else "System Active"
    val accentColor: String get() = if (isPersian) "رنگ شاخص" else "Accent Color"
    val selectedSuffix: String get() = if (isPersian) "(انتخاب‌شده)" else "(Selected)"
    val notificationsAndReminders: String get() = if (isPersian) "اعلان‌ها و یادآورها" else "NOTIFICATIONS & REMINDERS"
    val taskReminders: String get() = if (isPersian) "یادآور وظایف" else "Task Reminders"
    val taskRemindersDesc: String get() = if (isPersian) "اعلان‌های پوش و نوار وضعیت" else "Push notifications and status bar"
    val dailyBriefing: String get() = if (isPersian) "گزارش روزانه" else "Daily Briefing"
    val dailyBriefingDesc: String get() = if (isPersian) "هر روز ساعت ۰۸:۰۰" else "Every day at 08:00 AM"
    val dataAndIntegrations: String get() = if (isPersian) "داده‌ها و یکپارچه‌سازی" else "DATA & INTEGRATIONS"
    val googleCalendarSync: String get() = if (isPersian) "همگام‌سازی با تقویم گوگل" else "Google Calendar Sync"
    val googleCalendarSyncDesc: String get() = if (isPersian) "متصل (۲ تقویم)" else "Connected (2 calendars)"
    val backupAndExport: String get() = if (isPersian) "پشتیبان‌گیری و خروجی" else "Backup & Export"
    val backupAndExportDesc: String get() = if (isPersian) "آخرین همگام‌سازی امروز ساعت ۱۱:۴۰" else "Last synced today at 11:40 AM"
    val about: String get() = if (isPersian) "درباره کرونوس" else "ABOUT"
    val version: String get() = if (isPersian) "نسخه" else "Version"
    val versionText: String get() = if (isPersian) "کرونوس v1.4.0 (بیلد ۳۸۴)" else "Chronos v1.4.0 (Build 384)"
    val latestBadge: String get() = if (isPersian) "جدیدترین" else "LATEST"
    val privacyPolicy: String get() = if (isPersian) "حریم خصوصی و شرایط" else "Privacy Policy & Terms"
    val privacyPolicyDesc: String get() = if (isPersian) "اطلاعات حقوقی و دسترسی‌ها" else "Legal disclosures & data rights"
}

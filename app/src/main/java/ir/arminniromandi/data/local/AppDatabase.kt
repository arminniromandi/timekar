package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import com.example.util.DateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronos_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.taskDao()?.insertAll(getInitialSeedTasks())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialSeedTasks(): List<TaskEntity> {
            val today = DateHelper.todayEpochDay()
            val yesterday = today - 1
            val tomorrow = today + 1
            val twoDaysLater = today + 2

            return listOf(
                // 1. Overdue Item (Yesterday)
                TaskEntity(
                    title = "Submit Q3 Tax Documentation",
                    description = "File corporate tax return and submit contractor receipts to accounting portal.",
                    dateEpochDay = yesterday,
                    startTimeMinute = -1,
                    endTimeMinute = -1,
                    isAllDay = true,
                    priority = "HIGH",
                    category = "Finance",
                    locationOrDetails = "Tax Portal",
                    reminderText = "1 day prior",
                    recurrenceText = "Quarterly",
                    isCompleted = false,
                    subtasksJson = """[{"id":"1","title":"Gather preliminary throughput metrics","isCompleted":false},{"id":"2","title":"Finalize invoices summary","isCompleted":false}]""",
                    iconType = "checklist"
                ),
                // 2. Anytime Today Task 1
                TaskEntity(
                    title = "Review Q4 design spec",
                    description = "Verify accessibility standards and dark mode color contrast ratios.",
                    dateEpochDay = today,
                    startTimeMinute = 14 * 60, // 14:00
                    endTimeMinute = -1,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Specs",
                    locationOrDetails = "Figma & Jira",
                    reminderText = "10 minutes prior",
                    recurrenceText = "Does not repeat",
                    isCompleted = false,
                    iconType = "none"
                ),
                // 3. Anytime Today Task 2
                TaskEntity(
                    title = "Call pharmacy",
                    description = "Refill monthly prescription and verify delivery window.",
                    dateEpochDay = today,
                    startTimeMinute = -1,
                    endTimeMinute = -1,
                    isAllDay = true,
                    priority = "LOW",
                    category = "Personal",
                    locationOrDetails = "MedExpress",
                    reminderText = "At time of event",
                    recurrenceText = "Does not repeat",
                    isCompleted = false,
                    iconType = "none"
                ),
                // 4. Today 09:00 - 09:45
                TaskEntity(
                    title = "Team sprint sync",
                    description = "Standup with team, unblock frontend blockers, review burndown charts.",
                    dateEpochDay = today,
                    startTimeMinute = 9 * 60, // 09:00
                    endTimeMinute = 9 * 60 + 45, // 09:45
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Product",
                    locationOrDetails = "Video Room 2",
                    reminderText = "10 minutes prior via system banner",
                    recurrenceText = "Weekly on Thursday",
                    isCompleted = false,
                    iconType = "group"
                ),
                // 5. Today 11:30 - 12:30
                TaskEntity(
                    title = "Deep work: Architecture doc",
                    description = "Refine Clean Architecture and MVVM patterns with Room persistence.",
                    dateEpochDay = today,
                    startTimeMinute = 11 * 60 + 30, // 11:30
                    endTimeMinute = 12 * 60 + 30, // 12:30
                    isAllDay = false,
                    priority = "MEDIUM",
                    category = "Architecture",
                    locationOrDetails = "Draft v1.2",
                    reminderText = "15 minutes prior",
                    recurrenceText = "Weekly on Thursday",
                    isCompleted = false,
                    iconType = "edit_note"
                ),
                // 6. Today 13:00 - 13:30
                TaskEntity(
                    title = "Quick sync with Elena",
                    description = "Discuss user test feedback regarding timeline gutter ergonomics.",
                    dateEpochDay = today,
                    startTimeMinute = 13 * 60, // 13:00
                    endTimeMinute = 13 * 60 + 30, // 13:30
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "General",
                    locationOrDetails = "Phone Call",
                    reminderText = "5 minutes prior",
                    recurrenceText = "Does not repeat",
                    isCompleted = false,
                    iconType = "call"
                ),
                // 7. Today 14:00 - 15:00 (High priority review with team from Modal Sheet screenshot)
                TaskEntity(
                    title = "Architecture review with team",
                    description = "Coordinate with Lead System Architect regarding low-latency sync logic and database migrations.",
                    dateEpochDay = today,
                    startTimeMinute = 14 * 60, // 14:00
                    endTimeMinute = 15 * 60, // 15:00
                    isAllDay = false,
                    priority = "HIGH",
                    category = "Product Core",
                    locationOrDetails = "Meet • Lead Architect + 4",
                    reminderText = "10 minutes prior via system banner",
                    recurrenceText = "Weekly on Thursday",
                    isCompleted = false,
                    subtasksJson = """[{"id":"s1","title":"Gather preliminary throughput metrics","isCompleted":true},{"id":"s2","title":"Finalize architectural schema RFC","isCompleted":false}]""",
                    iconType = "meet"
                ),
                // 8. Today 15:00 Completed Task
                TaskEntity(
                    title = "Prepare release checklist",
                    description = "Audit permissions, proguard rules, and release signing keys.",
                    dateEpochDay = today,
                    startTimeMinute = 15 * 60,
                    endTimeMinute = 15 * 60 + 30,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Eng",
                    locationOrDetails = "CI/CD Pipeline",
                    reminderText = "None",
                    recurrenceText = "Does not repeat",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600000L,
                    iconType = "checklist"
                ),
                // 9. Today 16:30 - 17:00
                TaskEntity(
                    title = "Sprint planning catch-up",
                    description = "Review story point velocity and finalize commitment for next cycle.",
                    dateEpochDay = today,
                    startTimeMinute = 16 * 60 + 30,
                    endTimeMinute = 17 * 60,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "General",
                    locationOrDetails = "Async wrap",
                    reminderText = "10 minutes prior",
                    recurrenceText = "Weekly",
                    isCompleted = false,
                    iconType = "none"
                ),
                // 10. Upcoming (Tomorrow 10:00 AM)
                TaskEntity(
                    title = "Weekly Engineering Retro",
                    description = "What went well, what could be improved, action items.",
                    dateEpochDay = tomorrow,
                    startTimeMinute = 10 * 60,
                    endTimeMinute = 11 * 60,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Eng",
                    locationOrDetails = "Main Room",
                    reminderText = "15 minutes prior",
                    recurrenceText = "Weekly",
                    isCompleted = false,
                    iconType = "group"
                ),
                // 11. Upcoming (2 days later)
                TaskEntity(
                    title = "Prepare design system handoff",
                    description = "Export typography tokens, spacing scales, and M3 elevation guides.",
                    dateEpochDay = twoDaysLater,
                    startTimeMinute = 11 * 60 + 30,
                    endTimeMinute = 12 * 60 + 30,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Design",
                    locationOrDetails = "Design Portal",
                    reminderText = "30 minutes prior",
                    recurrenceText = "Does not repeat",
                    isCompleted = false,
                    iconType = "none"
                ),
                // 12. Completed historic task 1
                TaskEntity(
                    title = "Draft product release notes v2.4",
                    description = "Include changelog, bug fixes, and upgrade instructions.",
                    dateEpochDay = yesterday,
                    startTimeMinute = 9 * 60 + 15,
                    endTimeMinute = 9 * 60 + 45,
                    isAllDay = false,
                    priority = "LOW",
                    category = "Specs",
                    locationOrDetails = "Notion Docs",
                    reminderText = "None",
                    recurrenceText = "Does not repeat",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 86400000L,
                    iconType = "none"
                ),
                // 13. Completed historic task 2
                TaskEntity(
                    title = "Standup with mobile core team",
                    description = "Review architecture doc migration and DI setup.",
                    dateEpochDay = yesterday,
                    startTimeMinute = 9 * 60 + 30,
                    endTimeMinute = 10 * 60,
                    isAllDay = false,
                    priority = "NORMAL",
                    category = "Eng",
                    locationOrDetails = "Slack Huddle",
                    reminderText = "None",
                    recurrenceText = "Does not repeat",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 86400000L,
                    iconType = "group"
                )
            )
        }
    }
}

package ir.arminniromandi.timekar.domain.model

import java.util.UUID

data class SubtaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

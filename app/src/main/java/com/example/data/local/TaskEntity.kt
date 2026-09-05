package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val title: String,
    val description: String,
    val assignedAgentId: String,
    val assignedAgentName: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val progressPercent: Int = 0,
    val subStepsJson: String = "[]", // JSON array of step strings
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AgentStatus
import com.example.data.model.MessageType
import com.example.data.model.ModelProvider
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus

class FleetTypeConverters {
    @TypeConverter
    fun fromModelProvider(value: ModelProvider): String = value.name

    @TypeConverter
    fun toModelProvider(value: String): ModelProvider = try {
        ModelProvider.valueOf(value)
    } catch (e: Exception) {
        ModelProvider.GEMINI
    }

    @TypeConverter
    fun fromAgentStatus(value: AgentStatus): String = value.name

    @TypeConverter
    fun toAgentStatus(value: String): AgentStatus = try {
        AgentStatus.valueOf(value)
    } catch (e: Exception) {
        AgentStatus.IDLE
    }

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = try {
        TaskPriority.valueOf(value)
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (e: Exception) {
        TaskStatus.QUEUED
    }

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = try {
        MessageType.valueOf(value)
    } catch (e: Exception) {
        MessageType.AGENT
    }
}

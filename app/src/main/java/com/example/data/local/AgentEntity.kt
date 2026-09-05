package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AgentStatus
import com.example.data.model.ModelProvider

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val callSign: String,
    val role: String,
    val provider: ModelProvider,
    val modelName: String,
    val endpointUrl: String,
    val apiKey: String,
    val systemPrompt: String,
    val mcpToolsCsv: String, // comma separated tool IDs
    val status: AgentStatus = AgentStatus.IDLE,
    val colorHex: Long,
    val isCustom: Boolean = false,
    val totalTasksCompleted: Int = 0
)

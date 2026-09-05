package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderName: String,
    val senderRole: String,
    val senderType: MessageType,
    val content: String,
    val toolName: String? = null,
    val toolInput: String? = null,
    val toolOutput: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFF00E5FF,
    val agentCallSign: String? = null
)

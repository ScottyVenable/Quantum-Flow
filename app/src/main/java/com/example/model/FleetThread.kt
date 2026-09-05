package com.example.model

data class ToolCallDetails(
    val toolName: String,
    val commandOrInput: String,
    val output: String? = null,
    val isRunning: Boolean = false,
    val isSuccess: Boolean = true
)

data class WhiteboardSnapshotDetails(
    val title: String,
    val elementCount: Int,
    val strokeCount: Int,
    val noteCount: Int,
    val imageCount: Int,
    val contributors: List<String>,
    val summary: String
)

data class ChatMessage(
    val id: String,
    val threadId: String,
    val senderId: String, // agent id or "user"
    val senderName: String,
    val senderRole: String,
    val content: String,
    val timestamp: String,
    val toolCall: ToolCallDetails? = null,
    val whiteboardSnapshot: WhiteboardSnapshotDetails? = null,
    val isUser: Boolean = senderId == "user",
    val avatarColorIndex: Int = 0,
    val initials: String = "AI"
)

enum class TaskStatus(val label: String) {
    QUEUED("Queued"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("Review"),
    COMPLETED("Completed")
}

data class FleetThread(
    val id: String,
    val title: String,
    val timeRange: String, // e.g., "8:00 - 8:45 AM (UTC)"
    val dateLabel: String = "Fri 07", // e.g. "Wed 05", "Thu 06", "Fri 07"
    val category: String, // "Marketing", "Product Manager", "DevOps", "Engineering"
    val sourcePlatform: String, // "On Google Meet", "On Local Ollama", "On Zoom"
    val assignedAgentIds: List<String>,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    val progress: Float = 0.65f,
    val lastUpdate: String = "Just now",
    val description: String = ""
)

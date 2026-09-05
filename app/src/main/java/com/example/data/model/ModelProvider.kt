package com.example.data.model

enum class ModelProvider(val displayName: String, val defaultEndpoint: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com"),
    LOCAL_HOST("Local Host (Ollama/LM Studio)", "http://10.0.2.2:11434"),
    OPENAI_COMPATIBLE("OpenAI / vLLM / Groq", "https://api.openai.com/v1"),
    CUSTOM_REST("Custom REST API", "http://10.0.2.2:8000")
}

enum class AgentStatus(val label: String, val colorHex: Long) {
    IDLE("IDLE", 0xFF64748B),
    THINKING("THINKING", 0xFF00E5FF),
    EXECUTING_TOOL("MCP ACTIVE", 0xFFFFB300),
    REVIEWING("VERIFYING", 0xFF7C4DFF),
    COMPLETED("SYNCED", 0xFF00E676),
    OFFLINE("OFFLINE", 0xFF475569)
}

enum class TaskPriority(val label: String, val colorHex: Long) {
    CRITICAL("P0 - CRIT", 0xFFFF3D71),
    HIGH("P1 - HIGH", 0xFFFF9100),
    MEDIUM("P2 - MED", 0xFF00E5FF),
    LOW("P3 - LOW", 0xFF94A3B8)
}

enum class TaskStatus(val label: String, val colorHex: Long) {
    QUEUED("QUEUED", 0xFF64748B),
    IN_PROGRESS("IN PROGRESS", 0xFF00E5FF),
    MCP_EXEC("RUNNING TOOL", 0xFFFFB300),
    REVIEW("IN REVIEW", 0xFF7C4DFF),
    COMPLETED("COMPLETED", 0xFF00E676),
    FAILED("FAILED", 0xFFFF3D71)
}

enum class MessageType {
    USER,
    AGENT,
    SYSTEM,
    TOOL_EXECUTION
}

data class McpTool(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val isEnabled: Boolean = true
)

package com.example.model

data class TaskQueueItem(
    val id: String,
    val title: String,
    val threadId: String,
    val priority: String = "High", // "Critical", "High", "Medium", "Low"
    val assignedAgentId: String,
    val assignedAgentName: String,
    val status: TaskStatus,
    val progress: Float, // 0.0 to 1.0
    val executionNote: String = "Allocating GPU tensors & MCP sockets..."
)

data class LocalHostConfig(
    val endpointUrl: String = "http://10.0.2.2:11434",
    val defaultModel: String = "llama3.2:3b",
    val isConnected: Boolean = true,
    val latencyMs: Long = 34,
    val gpuAllocated: String = "VRAM 5.8 / 16 GB",
    val activeThreads: Int = 4,
    val availableModels: List<String> = listOf(
        "llama3.2:3b",
        "deepseek-r1:8b",
        "qwen2.5-coder:7b",
        "mistral:7b",
        "phi-3.5:mini"
    )
)

data class McpServer(
    val id: String,
    val name: String,
    val endpoint: String,
    val toolsCount: Int,
    val isConnected: Boolean,
    val description: String
)

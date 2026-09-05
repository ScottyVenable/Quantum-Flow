package com.example.model

// Fleet Agent Domain Models
enum class ModelProvider(val displayName: String) {
    LOCAL_HOST_OLLAMA("Local Ollama / LM Studio"),
    GEMINI_API("Gemini 2.5"),
    OPENAI_API("OpenAI / Compatible"),
    ANTHROPIC_API("Anthropic Claude"),
    GROK_XAI("xAI Grok")
}

enum class AgentStatus(val label: String) {
    IDLE("Idle"),
    THINKING("Thinking..."),
    CALLING_TOOL("Calling MCP Tool"),
    EXECUTING("Executing Task"),
    OFFLINE("Offline")
}

data class Agent(
    val id: String,
    val name: String,
    val role: String,
    val provider: ModelProvider,
    val modelName: String,
    val hostEndpoint: String = "http://10.0.2.2:11434",
    val apiKey: String = "",
    val mcpTools: List<String> = listOf("mcp:filesystem", "mcp:terminal", "mcp:web_search"),
    val status: AgentStatus = AgentStatus.IDLE,
    val avatarColorIndex: Int = 0,
    val initials: String = "AG",
    val currentTask: String? = null,
    val isLocal: Boolean = provider == ModelProvider.LOCAL_HOST_OLLAMA
)

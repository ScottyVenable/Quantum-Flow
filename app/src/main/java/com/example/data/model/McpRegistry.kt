package com.example.data.model

object McpRegistry {
    val AVAILABLE_TOOLS = listOf(
        McpTool(
            id = "mcp_web_search",
            name = "Web Intelligence (MCP)",
            description = "Live internet telemetry, documentation lookup & search index query",
            category = "NETWORK"
        ),
        McpTool(
            id = "mcp_code_sandbox",
            name = "Python Code Sandbox (MCP)",
            description = "Execute mathematical computations, algorithms & diagnostic scripts",
            category = "COMPUTE"
        ),
        McpTool(
            id = "mcp_file_system",
            name = "Workspace FS (MCP)",
            description = "Inspect directories, read configs, write mission reports & code artifacts",
            category = "STORAGE"
        ),
        McpTool(
            id = "mcp_db_analytics",
            name = "SQL & Data Pipeline (MCP)",
            description = "Query relational schemas, run time-series telemetry metrics & aggregates",
            category = "DATA"
        ),
        McpTool(
            id = "mcp_system_diagnostics",
            name = "Orbital Diagnostics (MCP)",
            description = "Inspect agent cluster status, latency, memory buffers & node telemetry",
            category = "SYSTEM"
        ),
        McpTool(
            id = "mcp_mission_delegate",
            name = "Fleet Delegation Protocol (MCP)",
            description = "Autonomous task dispatch and peer-to-peer agent coordination",
            category = "ORCHESTRATION"
        )
    )

    fun getToolById(id: String): McpTool? = AVAILABLE_TOOLS.find { it.id == id }
}

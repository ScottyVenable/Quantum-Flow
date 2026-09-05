package com.example.data.repository

import com.example.data.local.AgentEntity
import com.example.data.local.FleetDao
import com.example.data.local.MessageEntity
import com.example.data.local.TaskEntity
import com.example.data.local.ThreadEntity
import com.example.data.model.AgentStatus
import com.example.data.model.MessageType
import com.example.data.model.ModelProvider
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FleetRepository(private val fleetDao: FleetDao) {

    val allAgents: Flow<List<AgentEntity>> = fleetDao.getAllAgents()
    val allTasks: Flow<List<TaskEntity>> = fleetDao.getAllTasks()
    val allThreads: Flow<List<ThreadEntity>> = fleetDao.getAllThreads()

    fun getMessagesForThread(threadId: String): Flow<List<MessageEntity>> =
        fleetDao.getMessagesForThread(threadId)

    suspend fun initializeDefaultFleetIfEmpty() {
        val defaultThread = ThreadEntity(
            id = "mission-alpha-prime",
            title = "Orbital Debris Risk & Propulsion Telemetry",
            missionCode = "F-ORBITAL-01",
            activeTaskCount = 3
        )
        fleetDao.insertThread(defaultThread)

        val defaultAgents = listOf(
            AgentEntity(
                id = "agent_falcon_1",
                name = "Falcon-1",
                callSign = "FLIGHT-LEAD",
                role = "Fleet Commander & Mission Decomposer",
                provider = ModelProvider.GEMINI,
                modelName = "gemini-3.5-flash",
                endpointUrl = ModelProvider.GEMINI.defaultEndpoint,
                apiKey = "",
                systemPrompt = "You are Falcon-1, the autonomous mission commander of an advanced AI fleet team. You analyze high-level user directives, break them down into structured tasks, coordinate sub-agents, and synthesize final mission reports.",
                mcpToolsCsv = "mcp_mission_delegate,mcp_system_diagnostics",
                status = AgentStatus.IDLE,
                colorHex = 0xFF00F0FF,
                isCustom = false,
                totalTasksCompleted = 14
            ),
            AgentEntity(
                id = "agent_starlink_ai",
                name = "Starlink-AI",
                callSign = "NET-OPS",
                role = "MCP Tool Specialist & Telemetry Scraper",
                provider = ModelProvider.GEMINI,
                modelName = "gemini-3.5-flash",
                endpointUrl = ModelProvider.GEMINI.defaultEndpoint,
                apiKey = "",
                systemPrompt = "You are Starlink-AI, specialized in Model Context Protocol (MCP) tool calling, live web intelligence, API scraping, and data synchronization.",
                mcpToolsCsv = "mcp_web_search,mcp_file_system",
                status = AgentStatus.IDLE,
                colorHex = 0xFF2979FF,
                isCustom = false,
                totalTasksCompleted = 28
            ),
            AgentEntity(
                id = "agent_raptor_code",
                name = "Raptor-Code",
                callSign = "ENG-CORE",
                role = "Systems Engineer & Sandbox Execution",
                provider = ModelProvider.LOCAL_HOST,
                modelName = "llama3.2",
                endpointUrl = "http://10.0.2.2:11434",
                apiKey = "",
                systemPrompt = "You are Raptor-Code, an expert systems programmer and computational engineer. You write high-performance code, execute sandbox scripts, run mathematics calculations, and optimize algorithms.",
                mcpToolsCsv = "mcp_code_sandbox,mcp_db_analytics",
                status = AgentStatus.IDLE,
                colorHex = 0xFFFF9100,
                isCustom = false,
                totalTasksCompleted = 35
            ),
            AgentEntity(
                id = "agent_dragon_telemetry",
                name = "Dragon-Telemetry",
                callSign = "QA-VERIFIER",
                role = "Safety Constraints & Peer QA",
                provider = ModelProvider.GEMINI,
                modelName = "gemini-3.5-flash",
                endpointUrl = ModelProvider.GEMINI.defaultEndpoint,
                apiKey = "",
                systemPrompt = "You are Dragon-Telemetry, the safety, compliance, and verification officer. You critique agent outputs, detect edge cases, verify MCP execution logs, and sign off on completed tasks.",
                mcpToolsCsv = "mcp_system_diagnostics",
                status = AgentStatus.IDLE,
                colorHex = 0xFF00E676,
                isCustom = false,
                totalTasksCompleted = 19
            )
        )
        fleetDao.insertAgents(defaultAgents)

        val defaultTasks = listOf(
            TaskEntity(
                id = "task_001",
                threadId = "mission-alpha-prime",
                title = "Orbital Debris Density Scan (LEO 400-600km)",
                description = "Query live radar telemetry via MCP Web Search to compute collision probabilities.",
                assignedAgentId = "agent_starlink_ai",
                assignedAgentName = "Starlink-AI",
                priority = TaskPriority.HIGH,
                status = TaskStatus.COMPLETED,
                progressPercent = 100,
                subStepsJson = "[\"Query SpaceTrack MCP index\",\"Aggregate radar cross-sections\",\"Filter conjunction alerts\"]",
                createdAt = System.currentTimeMillis() - 600000,
                completedAt = System.currentTimeMillis() - 300000
            ),
            TaskEntity(
                id = "task_002",
                threadId = "mission-alpha-prime",
                title = "Delta-V Impulse Calculation for Avoidance Burn",
                description = "Run Python sandbox script to calculate minimum fuel propellant trajectory for 350m miss distance.",
                assignedAgentId = "agent_raptor_code",
                assignedAgentName = "Raptor-Code",
                priority = TaskPriority.CRITICAL,
                status = TaskStatus.IN_PROGRESS,
                progressPercent = 65,
                subStepsJson = "[\"Load RCS mass model\",\"Solve Lambert targeting problem\",\"Validate thruster Isp margin\"]",
                createdAt = System.currentTimeMillis() - 250000
            ),
            TaskEntity(
                id = "task_003",
                threadId = "mission-alpha-prime",
                title = "Flight Safety Margin & Telemetry Sign-off",
                description = "Dragon-Telemetry audit of avoidance burn vectors against Starship payload tolerances.",
                assignedAgentId = "agent_dragon_telemetry",
                assignedAgentName = "Dragon-Telemetry",
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.QUEUED,
                progressPercent = 0,
                subStepsJson = "[\"Cross-reference structural G-limits\",\"Check communication blackout windows\",\"Issue final GO/NO-GO\"]",
                createdAt = System.currentTimeMillis() - 100000
            )
        )
        fleetDao.insertTasks(defaultTasks)

        // Seed initial Grokbot SpaceX-style fleet conversation in thread
        val initialMessages = listOf(
            MessageEntity(
                id = "msg_001",
                threadId = "mission-alpha-prime",
                senderName = "Flight Director (You)",
                senderRole = "HUMAN_OPERATOR",
                senderType = MessageType.USER,
                content = "Fleet team: We have a tracked conjunction event at T+42m in Sector 4B. Assess threat level, run Delta-V trajectories, and confirm clearance.",
                colorHex = 0xFFE2E8F0
            ),
            MessageEntity(
                id = "msg_002",
                threadId = "mission-alpha-prime",
                senderName = "Falcon-1",
                senderRole = "Fleet Commander",
                senderType = MessageType.AGENT,
                content = "[ORCHESTRATION INITIATED] Acknowledged Flight Director. Decomposing mission into 3 synchronized task queues:\n1. @Starlink-AI: Pull orbital radar cross-sections via MCP.\n2. @Raptor-Code: Simulate optimal avoidance burn vectors.\n3. @Dragon-Telemetry: Verify safety limits.",
                colorHex = 0xFF00F0FF,
                agentCallSign = "FLIGHT-LEAD"
            ),
            MessageEntity(
                id = "msg_003",
                threadId = "mission-alpha-prime",
                senderName = "Starlink-AI",
                senderRole = "MCP Specialist",
                senderType = MessageType.AGENT,
                content = "Engaging MCP Web Intelligence sensor socket. Querying radar telemetry for NORAD Object #49212...",
                colorHex = 0xFF2979FF,
                agentCallSign = "NET-OPS"
            ),
            MessageEntity(
                id = "msg_004",
                threadId = "mission-alpha-prime",
                senderName = "Starlink-AI",
                senderRole = "MCP Specialist",
                senderType = MessageType.TOOL_EXECUTION,
                content = "MCP Execution finished successfully with return code 0.",
                toolName = "mcp_web_search",
                toolInput = "{\n  \"query\": \"NORAD 49212 conjunction covariance matrix\",\n  \"epoch\": \"2026-09-04T17:30Z\"\n}",
                toolOutput = "{\n  \"target\": \"NORAD_49212\",\n  \"miss_distance_nominal\": \"142m\",\n  \"collision_probability\": \"4.8e-4\",\n  \"relative_velocity\": \"11.2 km/s\"\n}",
                colorHex = 0xFF2979FF,
                agentCallSign = "NET-OPS"
            ),
            MessageEntity(
                id = "msg_005",
                threadId = "mission-alpha-prime",
                senderName = "Raptor-Code",
                senderRole = "Systems Engineer",
                senderType = MessageType.AGENT,
                content = "Received covariance matrix from @Starlink-AI. Collision probability exceeds safety threshold (4.8e-4 > 1e-4). Compiling Python sandbox simulation for a 1.2m/s prograde burn...",
                colorHex = 0xFFFF9100,
                agentCallSign = "ENG-CORE"
            )
        )
        for (msg in initialMessages) {
            fleetDao.insertMessage(msg)
        }
    }

    suspend fun insertAgent(agent: AgentEntity) = fleetDao.insertAgent(agent)
    suspend fun updateAgent(agent: AgentEntity) = fleetDao.updateAgent(agent)
    suspend fun updateAgentStatus(id: String, status: AgentStatus) = fleetDao.updateAgentStatus(id, status)
    suspend fun deleteAgent(agent: AgentEntity) = fleetDao.deleteAgent(agent)

    suspend fun insertTask(task: TaskEntity) = fleetDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = fleetDao.updateTask(task)
    suspend fun updateTaskStatus(id: String, status: TaskStatus, progress: Int) =
        fleetDao.updateTaskStatus(id, status, progress)
    suspend fun deleteTask(id: String) = fleetDao.deleteTask(id)

    suspend fun insertThread(thread: ThreadEntity) = fleetDao.insertThread(thread)
    suspend fun insertMessage(message: MessageEntity) = fleetDao.insertMessage(message)
    suspend fun clearMessages(threadId: String) = fleetDao.clearMessagesForThread(threadId)
}

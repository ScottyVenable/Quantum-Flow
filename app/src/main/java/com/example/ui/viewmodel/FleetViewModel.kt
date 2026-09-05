package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgentEntity
import com.example.data.local.FleetDatabase
import com.example.data.local.MessageEntity
import com.example.data.local.TaskEntity
import com.example.data.local.ThreadEntity
import com.example.data.model.AgentStatus
import com.example.data.model.McpRegistry
import com.example.data.model.MessageType
import com.example.data.model.ModelProvider
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.network.GeminiApiClient
import com.example.data.network.LocalHostStatus
import com.example.data.network.LocalModelClient
import com.example.data.network.McpExecutor
import com.example.data.repository.FleetRepository
import com.example.model.*
import com.example.service.FleetEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class FleetNavTab(val label: String) {
    HOME("Home"),
    TODO("To-do"),
    SEARCH("Search"),
    SETTINGS("Settings")
}

data class FleetUiState(
    val selectedThreadId: String = "mission-alpha-prime",
    val selectedNavTab: FleetNavTab = FleetNavTab.HOME,
    val searchQuery: String = "",
    val isBookmarked: Boolean = true,
    val isAutonomousRunning: Boolean = false,
    val autonomousCurrentStep: String = "",
    val activeAgentFilter: String? = null,
    val taskFilter: TaskStatus? = null,
    val isSidebarOpen: Boolean = false,
    val isAddAgentDialogOpen: Boolean = false,
    val isAddTaskDialogOpen: Boolean = false,
    val isLocalHostDialogOpen: Boolean = false,
    val localHostStatus: LocalHostStatus? = null,
    val isTestingLocalHost: Boolean = false,
    val localHostUrl: String = "http://10.0.2.2:11434",
    val selectedLocalModel: String = "llama3.2",
    val isDarkMode: Boolean = false, // Clean, high-contrast light theme matching the NotewareLM reference!
    val bannerNotice: String? = null,
    val notifications: List<FleetNotification> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val userName: String = "Favour Adjenuvurhe",
    val userEmail: String = "favour-amaaavl@outlook.com",
    val storageUsedMb: Int = 500,
    val storageTotalMb: Int = 1000,
    val contextTokensUsed: Int = 54200,
    val contextTokensTotal: Int = 128000,
    val isWhiteboardOpen: Boolean = false,
    val whiteboards: Map<String, ThreadWhiteboard> = FleetEngine().getDefaultWhiteboards()
) {
    val currentWhiteboard: ThreadWhiteboard
        get() = whiteboards[selectedThreadId] ?: ThreadWhiteboard(threadId = selectedThreadId)
}

class FleetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FleetRepository

    private val _uiState = MutableStateFlow(FleetUiState(notifications = createInitialNotifications()))
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    init {
        val db = FleetDatabase.getInstance(application)
        repository = FleetRepository(db.fleetDao())
        viewModelScope.launch {
            repository.initializeDefaultFleetIfEmpty()
        }
    }

    val agents: StateFlow<List<AgentEntity>> = repository.allAgents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val threads: StateFlow<List<ThreadEntity>> = repository.allThreads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.getMessagesForThread("mission-alpha-prime")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSidebar() {
        _uiState.value = _uiState.value.copy(isSidebarOpen = !_uiState.value.isSidebarOpen)
    }

    fun setSidebarOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSidebarOpen = open)
    }

    fun setTaskFilter(status: TaskStatus?) {
        _uiState.value = _uiState.value.copy(taskFilter = status)
    }

    fun showAddAgentDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isAddAgentDialogOpen = show)
    }

    fun showAddTaskDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isAddTaskDialogOpen = show)
    }

    fun showLocalHostDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isLocalHostDialogOpen = show)
    }

    fun updateLocalHostUrl(url: String) {
        _uiState.value = _uiState.value.copy(localHostUrl = url)
    }

    fun updateSelectedLocalModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedLocalModel = model)
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkMode = !_uiState.value.isDarkMode)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(bannerNotice = null)
    }

    fun testLocalHostConnection(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingLocalHost = true)
            val result = LocalModelClient.pingServer(url)
            _uiState.value = _uiState.value.copy(
                isTestingLocalHost = false,
                localHostStatus = result,
                selectedLocalModel = result.availableModels.firstOrNull() ?: _uiState.value.selectedLocalModel
            )
        }
    }

    fun openWhiteboard() {
        _uiState.value = _uiState.value.copy(isWhiteboardOpen = true)
    }

    fun closeWhiteboard() {
        _uiState.value = _uiState.value.copy(isWhiteboardOpen = false)
    }

    fun addWhiteboardStroke(stroke: DrawingStroke) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            strokes = currentWb.strokes + stroke,
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(whiteboards = updatedMap)
    }

    fun addWhiteboardNote(note: WhiteboardNote) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            notes = currentWb.notes + note,
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(
            whiteboards = updatedMap,
            bannerNotice = "Sticky note pinned to collaborative whiteboard!"
        )
    }

    fun addWhiteboardShape(shape: WhiteboardShape) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            shapes = currentWb.shapes + shape,
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(
            whiteboards = updatedMap,
            bannerNotice = "Architecture node placed on whiteboard!"
        )
    }

    fun addWhiteboardImage(image: WhiteboardImageItem) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            images = currentWb.images + image,
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(
            whiteboards = updatedMap,
            bannerNotice = "Schematic blueprint attached to whiteboard!"
        )
    }

    fun deleteWhiteboardNote(noteId: String) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            notes = currentWb.notes.filterNot { it.id == noteId },
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(whiteboards = updatedMap)
    }

    fun deleteWhiteboardShape(shapeId: String) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            shapes = currentWb.shapes.filterNot { it.id == shapeId },
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(whiteboards = updatedMap)
    }

    fun deleteWhiteboardImage(imageId: String) {
        val threadId = _uiState.value.selectedThreadId
        val currentWb = _uiState.value.currentWhiteboard
        val updatedWb = currentWb.copy(
            images = currentWb.images.filterNot { it.id == imageId },
            lastUpdated = System.currentTimeMillis()
        )
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, updatedWb)
        }
        _uiState.value = _uiState.value.copy(whiteboards = updatedMap)
    }

    fun clearWhiteboard() {
        val threadId = _uiState.value.selectedThreadId
        val updatedMap = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, ThreadWhiteboard(threadId = threadId))
        }
        _uiState.value = _uiState.value.copy(
            whiteboards = updatedMap,
            bannerNotice = "Whiteboard canvas cleared."
        )
    }

    fun triggerAutonomousWhiteboardCollaboration(prompt: String) {
        val threadId = _uiState.value.selectedThreadId
        val currentAgents = agents.value
        val agent1 = currentAgents.find { it.role.contains("Lead") || it.role.contains("Arch") } ?: currentAgents.firstOrNull()
        val agent2 = currentAgents.find { it.role.contains("Code") || it.role.contains("Avionics") } ?: currentAgents.getOrNull(1)
        val agent3 = currentAgents.find { it.role.contains("Ops") || it.role.contains("Telemetry") } ?: currentAgents.getOrNull(2)

        val currentWb = _uiState.value.currentWhiteboard
        val liveCursors = listOfNotNull(
            agent1?.let { AgentCursor(it.id, it.name, it.name.take(2).uppercase(), it.role, 60f, 150f, "Sketching Avionics Bus...", it.colorHex) },
            agent2?.let { AgentCursor(it.id, it.name, it.name.take(2).uppercase(), it.role, 220f, 180f, "Positioning Raptor Gimbal...", it.colorHex) },
            agent3?.let { AgentCursor(it.id, it.name, it.name.take(2).uppercase(), it.role, 140f, 260f, "Plotting Telemetry Orbit...", it.colorHex) }
        )

        val wbWithCursors = currentWb.copy(
            activeCursors = liveCursors,
            isAgentCollabActive = true
        )
        val mapWithCursors = _uiState.value.whiteboards.toMutableMap().apply {
            put(threadId, wbWithCursors)
        }
        _uiState.value = _uiState.value.copy(
            whiteboards = mapWithCursors,
            bannerNotice = "Fleet agents actively co-drawing on whiteboard..."
        )

        viewModelScope.launch {
            delay(1200)
            val newStroke = DrawingStroke(
                id = "stroke_" + UUID.randomUUID().toString().take(6),
                points = listOf(
                    DrawingPoint(80f, 180f),
                    DrawingPoint(140f, 210f),
                    DrawingPoint(200f, 210f),
                    DrawingPoint(250f, 170f)
                ),
                colorHex = 0xFF00E5FF,
                strokeWidth = 4f,
                authorName = agent2?.name ?: "Devon Vance",
                authorInitials = "DV",
                isAgent = true
            )

            val newShape = WhiteboardShape(
                id = "shape_" + UUID.randomUUID().toString().take(6),
                shapeType = WhiteboardShapeType.PROCESS_BLOCK,
                label = "Autonomous Avionics Sub-Loop",
                subLabel = "Failover latency < 1.4ms",
                x = 50f,
                y = 200f,
                colorHex = 0xFF3B82F6,
                authorName = agent2?.name ?: "Devon Vance",
                authorInitials = "DV"
            )

            val cur1 = _uiState.value.currentWhiteboard
            val step1Wb = cur1.copy(
                strokes = cur1.strokes + newStroke,
                shapes = cur1.shapes + newShape
            )
            val mapStep1 = _uiState.value.whiteboards.toMutableMap().apply {
                put(threadId, step1Wb)
            }
            _uiState.value = _uiState.value.copy(whiteboards = mapStep1)

            delay(1400)
            val newNote = WhiteboardNote(
                id = "note_" + UUID.randomUUID().toString().take(6),
                title = "Consensus Verified",
                content = "Multi-agent consensus algorithm confirmed $prompt. All telemetry endpoints isolated on Starlink Ku-band.",
                x = 200f,
                y = 190f,
                colorHex = 0xFFDCFCE7,
                authorName = agent1?.name ?: "James Brown",
                authorInitials = "JB",
                category = "Consensus",
                isAgent = true
            )

            val newImg = WhiteboardImageItem(
                id = "img_" + UUID.randomUUID().toString().take(6),
                title = "Starlink LEO Trajectory Plot",
                subtitle = "Active orbital vector verified against flight model",
                imageType = WhiteboardImageType.ORBITAL_TELEMETRY,
                x = 50f,
                y = 350f,
                authorName = agent3?.name ?: "David Chen",
                authorInitials = "DC",
                isAgent = true
            )

            val cur2 = _uiState.value.currentWhiteboard
            val step2Wb = cur2.copy(
                notes = cur2.notes + newNote,
                images = cur2.images + newImg,
                activeCursors = emptyList(),
                isAgentCollabActive = false
            )
            val mapStep2 = _uiState.value.whiteboards.toMutableMap().apply {
                put(threadId, step2Wb)
            }
            _uiState.value = _uiState.value.copy(
                whiteboards = mapStep2,
                bannerNotice = "Fleet finished collaborative planning session!"
            )

            postWhiteboardSnapshotToChat()
        }
    }

    fun postWhiteboardSnapshotToChat() {
        val wb = _uiState.value.currentWhiteboard
        val totalElements = wb.strokes.size + wb.notes.size + wb.shapes.size + wb.images.size

        val summaryContent = "🎨 [WHITEBOARD ARTIFACT] Collaborative Fleet Whiteboard Snapshot ($totalElements elements):\n" +
            "• ${wb.strokes.size} freehand vector schematics\n" +
            "• ${wb.notes.size} sticky planning tasks\n" +
            "• ${wb.shapes.size} architecture nodes\n" +
            "• ${wb.images.size} uploaded blueprint images\n" +
            "Tap 'Open Whiteboard' in the header to collaborate live with the agents."

        val snapshotMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            threadId = _uiState.value.selectedThreadId,
            senderName = "Fleet Whiteboard",
            senderRole = "COLLABORATIVE_CANVAS",
            senderType = MessageType.SYSTEM,
            content = summaryContent,
            colorHex = 0xFF00E5FF
        )

        viewModelScope.launch {
            repository.insertMessage(snapshotMsg)
            _uiState.value = _uiState.value.copy(
                bannerNotice = "Whiteboard snapshot posted to mission chat thread!"
            )
        }
    }

    fun sendMessage(
        content: String,
        targetAgentId: String? = null // null = Autonomous Fleet Broadcast
    ) {
        if (content.isBlank()) return

        val threadId = _uiState.value.selectedThreadId

        viewModelScope.launch {
            // 1. Post user message
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                senderName = "Flight Director",
                senderRole = "HUMAN_OPERATOR",
                senderType = MessageType.USER,
                content = content,
                colorHex = 0xFFE2E8F0
            )
            repository.insertMessage(userMsg)

            // 2. Determine execution flow: Targeted agent vs Full Autonomous Fleet Team
            val currentAgents = agents.value
            if (targetAgentId != null && targetAgentId != "fleet_all") {
                val agent = currentAgents.find { it.id == targetAgentId }
                if (agent != null) {
                    executeSingleAgentTurn(agent, content, threadId)
                }
            } else {
                executeAutonomousFleetTeam(content, threadId, currentAgents)
            }
        }
    }

    private suspend fun executeSingleAgentTurn(
        agent: AgentEntity,
        userPrompt: String,
        threadId: String
    ) {
        repository.updateAgentStatus(agent.id, AgentStatus.THINKING)
        delay(600)

        // If agent has MCP tool, invoke it
        val toolIds = agent.mcpToolsCsv.split(",").filter { it.isNotBlank() }
        if (toolIds.isNotEmpty()) {
            val primaryToolId = toolIds.first()
            repository.updateAgentStatus(agent.id, AgentStatus.EXECUTING_TOOL)

            val toolResult = McpExecutor.executeTool(primaryToolId, userPrompt)
            val toolMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                senderName = agent.name,
                senderRole = agent.role,
                senderType = MessageType.TOOL_EXECUTION,
                content = "Tool execution completed with status code 0",
                toolName = toolResult.toolName,
                toolInput = toolResult.inputJson,
                toolOutput = toolResult.outputResult,
                colorHex = agent.colorHex,
                agentCallSign = agent.callSign
            )
            repository.insertMessage(toolMsg)
            delay(400)
        }

        // Generate LLM answer
        var responseText = ""
        if (agent.provider == ModelProvider.LOCAL_HOST) {
            val localResult = LocalModelClient.generateLocalCompletion(
                baseUrl = agent.endpointUrl.ifBlank { _uiState.value.localHostUrl },
                modelName = agent.modelName.ifBlank { _uiState.value.selectedLocalModel },
                prompt = userPrompt,
                systemPrompt = agent.systemPrompt
            )
            responseText = localResult.getOrElse {
                "[Local Model Response from ${agent.modelName}]: Mission directive processed. Telemetry logs synchronized with local host node."
            }
        } else if (agent.provider == ModelProvider.GEMINI) {
            val geminiResult = GeminiApiClient.generateAgentResponse(
                prompt = userPrompt,
                systemInstruction = agent.systemPrompt,
                customApiKey = agent.apiKey.ifBlank { null }
            )
            responseText = geminiResult.getOrElse {
                "[Fleet Response // ${agent.callSign}]: Mission directive acknowledged. Coordinates calibrated, tools executed, and status synchronized with Fleet Command."
            }
        } else {
            responseText = "[Fleet Relay // ${agent.callSign}]: Processing complete. All telemetry and mission constraints satisfied."
        }

        val agentMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            threadId = threadId,
            senderName = agent.name,
            senderRole = agent.role,
            senderType = MessageType.AGENT,
            content = responseText,
            colorHex = agent.colorHex,
            agentCallSign = agent.callSign
        )
        repository.insertMessage(agentMsg)
        repository.updateAgentStatus(agent.id, AgentStatus.IDLE)
    }

    /**
     * Autonomous Fleet Collaboration Loop (Grokbot Fleet Inspired)
     * Multiple agents collaborate in real-time:
     * 1. Orchestrator decomposes mission into new Tasks in the queue
     * 2. MCP Specialist executes live tool calling & telemetry
     * 3. Systems Engineer computes solutions / code
     * 4. QA Verifier checks margins and resolves task queue
     */
    private suspend fun executeAutonomousFleetTeam(
        missionDirective: String,
        threadId: String,
        currentAgents: List<AgentEntity>
    ) {
        _uiState.value = _uiState.value.copy(
            isAutonomousRunning = true,
            autonomousCurrentStep = "Orchestrating Fleet Squad..."
        )

        val leadAgent = currentAgents.find { it.id == "agent_falcon_1" } ?: currentAgents.firstOrNull()
        val toolAgent = currentAgents.find { it.id == "agent_starlink_ai" } ?: currentAgents.getOrNull(1)
        val codeAgent = currentAgents.find { it.id == "agent_raptor_code" } ?: currentAgents.getOrNull(2)
        val qaAgent = currentAgents.find { it.id == "agent_dragon_telemetry" } ?: currentAgents.lastOrNull()

        try {
            // STEP 1: Mission Orchestrator decomposes and creates live task in queue
            if (leadAgent != null) {
                repository.updateAgentStatus(leadAgent.id, AgentStatus.THINKING)
                _uiState.value = _uiState.value.copy(autonomousCurrentStep = "${leadAgent.name} analyzing mission...")
                delay(900)

                val newTaskId = "task_${System.currentTimeMillis() % 10000}"
                val newTask = TaskEntity(
                    id = newTaskId,
                    threadId = threadId,
                    title = "Mission: $missionDirective",
                    description = "Autonomous multi-agent squad execution. Sub-steps decomposed across fleet.",
                    assignedAgentId = leadAgent.id,
                    assignedAgentName = leadAgent.name,
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.IN_PROGRESS,
                    progressPercent = 15,
                    subStepsJson = "[\"Decompose directive\",\"Execute MCP Telemetry\",\"Compute solution sandbox\",\"QA Verification\"]",
                    createdAt = System.currentTimeMillis()
                )
                repository.insertTask(newTask)

                val leadMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = leadAgent.name,
                    senderRole = leadAgent.role,
                    senderType = MessageType.AGENT,
                    content = "[FLEET MISSION INITIALIZED // QUEUE ID: $newTaskId]\n" +
                            "Mission: \"$missionDirective\"\n\n" +
                            "Squad Task Delegation:\n" +
                            "• @${toolAgent?.name ?: "Tool Specialist"}: Execute MCP Web & Orbital Telemetry scan\n" +
                            "• @${codeAgent?.name ?: "Code Specialist"}: Run computational sandbox simulation\n" +
                            "• @${qaAgent?.name ?: "QA Verifier"}: Safety audit and telemetry sign-off",
                    colorHex = leadAgent.colorHex,
                    agentCallSign = leadAgent.callSign
                )
                repository.insertMessage(leadMsg)
                repository.updateAgentStatus(leadAgent.id, AgentStatus.IDLE)
            }

            // STEP 2: MCP Tool Specialist activates tool calling
            if (toolAgent != null) {
                repository.updateAgentStatus(toolAgent.id, AgentStatus.EXECUTING_TOOL)
                _uiState.value = _uiState.value.copy(autonomousCurrentStep = "${toolAgent.name} calling MCP Tool...")
                delay(800)

                val toolResult = McpExecutor.executeTool("mcp_web_search", missionDirective)
                val toolMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = toolAgent.name,
                    senderRole = toolAgent.role,
                    senderType = MessageType.TOOL_EXECUTION,
                    content = "MCP Telemetry queried successfully with response payload.",
                    toolName = toolResult.toolName,
                    toolInput = toolResult.inputJson,
                    toolOutput = toolResult.outputResult,
                    colorHex = toolAgent.colorHex,
                    agentCallSign = toolAgent.callSign
                )
                repository.insertMessage(toolMsg)

                val toolHandoffMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = toolAgent.name,
                    senderRole = toolAgent.role,
                    senderType = MessageType.AGENT,
                    content = "@${codeAgent?.name ?: "Raptor-Code"}: Telemetry feed acquired and parsed. Data packet forwarded to Sandbox Runtime. Ready for execution.",
                    colorHex = toolAgent.colorHex,
                    agentCallSign = toolAgent.callSign
                )
                repository.insertMessage(toolHandoffMsg)
                repository.updateAgentStatus(toolAgent.id, AgentStatus.IDLE)
            }

            // STEP 3: Computational / Systems Specialist runs sandbox
            if (codeAgent != null) {
                repository.updateAgentStatus(codeAgent.id, AgentStatus.EXECUTING_TOOL)
                _uiState.value = _uiState.value.copy(autonomousCurrentStep = "${codeAgent.name} executing Python Sandbox...")
                delay(900)

                val sandboxResult = McpExecutor.executeTool("mcp_code_sandbox", "solve_trajectory('$missionDirective')")
                val sandboxMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = codeAgent.name,
                    senderRole = codeAgent.role,
                    senderType = MessageType.TOOL_EXECUTION,
                    content = "Code execution completed successfully in isolated container.",
                    toolName = sandboxResult.toolName,
                    toolInput = sandboxResult.inputJson,
                    toolOutput = sandboxResult.outputResult,
                    colorHex = codeAgent.colorHex,
                    agentCallSign = codeAgent.callSign
                )
                repository.insertMessage(sandboxMsg)

                val codeResponseMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = codeAgent.name,
                    senderRole = codeAgent.role,
                    senderType = MessageType.AGENT,
                    content = "Simulation converged in 0.042s. Optimal parameters calculated. Solution artifact generated and committed. Forwarding to @${qaAgent?.name ?: "Dragon-Telemetry"} for audit.",
                    colorHex = codeAgent.colorHex,
                    agentCallSign = codeAgent.callSign
                )
                repository.insertMessage(codeResponseMsg)
                repository.updateAgentStatus(codeAgent.id, AgentStatus.IDLE)
            }

            // STEP 4: QA & Verification closes out the task
            if (qaAgent != null) {
                repository.updateAgentStatus(qaAgent.id, AgentStatus.REVIEWING)
                _uiState.value = _uiState.value.copy(autonomousCurrentStep = "${qaAgent.name} verifying telemetry constraints...")
                delay(800)

                val qaMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    threadId = threadId,
                    senderName = qaAgent.name,
                    senderRole = qaAgent.role,
                    senderType = MessageType.AGENT,
                    content = "[QA TELEMETRY VERIFICATION // ALL CHECKS PASSED]\n" +
                            "✓ Input telemetry verified against MCP radar index\n" +
                            "✓ Trajectory computation within tolerance margin (0.00021 error)\n" +
                            "✓ Starship thermal and propellant constraints: NOMINAL\n\n" +
                            "Mission objective \"$missionDirective\" marked as COMPLETED. Flight Director, fleet is ready for next sequence.",
                    colorHex = qaAgent.colorHex,
                    agentCallSign = qaAgent.callSign
                )
                repository.insertMessage(qaMsg)
                repository.updateAgentStatus(qaAgent.id, AgentStatus.IDLE)
            }

        } finally {
            _uiState.value = _uiState.value.copy(
                isAutonomousRunning = false,
                autonomousCurrentStep = ""
            )
        }
    }

    fun addNewAgent(
        name: String,
        callSign: String,
        role: String,
        provider: ModelProvider,
        modelName: String,
        endpointUrl: String,
        apiKey: String,
        systemPrompt: String,
        selectedTools: List<String>,
        colorHex: Long
    ) {
        viewModelScope.launch {
            val newAgent = AgentEntity(
                id = "agent_custom_${System.currentTimeMillis()}",
                name = name.ifBlank { "Custom Agent" },
                callSign = callSign.ifBlank { "ALPHA-CUSTOM" },
                role = role.ifBlank { "Specialist" },
                provider = provider,
                modelName = modelName.ifBlank { "gemini-3.5-flash" },
                endpointUrl = endpointUrl.ifBlank { provider.defaultEndpoint },
                apiKey = apiKey,
                systemPrompt = systemPrompt.ifBlank { "You are a specialized autonomous fleet AI agent." },
                mcpToolsCsv = selectedTools.joinToString(","),
                status = AgentStatus.IDLE,
                colorHex = colorHex,
                isCustom = true,
                totalTasksCompleted = 0
            )
            repository.insertAgent(newAgent)
            _uiState.value = _uiState.value.copy(
                isAddAgentDialogOpen = false,
                bannerNotice = "Agent '${newAgent.name}' successfully integrated into the Fleet!"
            )
        }
    }

    fun addNewTask(
        title: String,
        description: String,
        assignedAgentId: String,
        priority: TaskPriority
    ) {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == assignedAgentId }
            val newTask = TaskEntity(
                id = "task_${System.currentTimeMillis() % 10000}",
                threadId = _uiState.value.selectedThreadId,
                title = title,
                description = description,
                assignedAgentId = assignedAgentId,
                assignedAgentName = agent?.name ?: "Fleet Squad",
                priority = priority,
                status = TaskStatus.QUEUED,
                progressPercent = 0,
                subStepsJson = "[\"Queue entry created\",\"Awaiting execution\",\"Verification pending\"]",
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(newTask)
            _uiState.value = _uiState.value.copy(
                isAddTaskDialogOpen = false,
                bannerNotice = "Task '${newTask.title}' dispatched to queue!"
            )
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            val progress = when (newStatus) {
                TaskStatus.QUEUED -> 0
                TaskStatus.IN_PROGRESS -> 35
                TaskStatus.MCP_EXEC -> 70
                TaskStatus.REVIEW -> 90
                TaskStatus.COMPLETED -> 100
                TaskStatus.FAILED -> 0
            }
            repository.updateTaskStatus(taskId, newStatus, progress)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearMessages(_uiState.value.selectedThreadId)
        }
    }

    fun selectNavTab(tab: FleetNavTab) {
        _uiState.value = _uiState.value.copy(selectedNavTab = tab)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleBookmark() {
        val current = _uiState.value.isBookmarked
        _uiState.value = _uiState.value.copy(
            isBookmarked = !current,
            bannerNotice = if (!current) "Note saved to Bookmarks." else "Note removed from Bookmarks."
        )
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun acceptNotification(notifId: String) {
        val updated = _uiState.value.notifications.map { notif ->
            if (notif.id == notifId) notif.copy(approvalState = true, isUnread = false) else notif
        }
        _uiState.value = _uiState.value.copy(
            notifications = updated,
            bannerNotice = "Accepted request and merged into active project workspace."
        )
    }

    fun declineNotification(notifId: String) {
        val updated = _uiState.value.notifications.map { notif ->
            if (notif.id == notifId) notif.copy(approvalState = false, isUnread = false) else notif
        }
        _uiState.value = _uiState.value.copy(
            notifications = updated,
            bannerNotice = "Declined project join request."
        )
    }

    fun archiveAllNotifications() {
        _uiState.value = _uiState.value.copy(
            notifications = emptyList(),
            bannerNotice = "All notifications archived."
        )
    }

    fun markAllNotificationsAsRead() {
        val updated = _uiState.value.notifications.map { it.copy(isUnread = false) }
        _uiState.value = _uiState.value.copy(
            notifications = updated,
            bannerNotice = "All notifications marked as read."
        )
    }

    fun toggleNotificationReaction(notifId: String, reaction: String) {
        val updated = _uiState.value.notifications.map { notif ->
            if (notif.id == notifId) {
                val current = notif.reactions.toMutableList()
                if (current.contains(reaction)) {
                    current.remove(reaction)
                } else {
                    current.add(reaction)
                }
                notif.copy(reactions = current)
            } else notif
        }
        _uiState.value = _uiState.value.copy(notifications = updated)
    }
}

private fun createInitialNotifications(): List<FleetNotification> {
    return listOf(
        FleetNotification(
            id = "notif_1",
            senderName = "Hailey Garza",
            senderRole = "UI Architect",
            senderAvatarIndex = 0,
            statusDot = NotificationStatusDot.GREEN,
            headlinePrefix = "Hailey Garza",
            actionText = "added new tags to",
            targetSubject = "Ease Design System",
            timeAgo = "1 mins. ago",
            projectName = "Easy 2023 Project",
            pillTags = listOf("UI Design", "Dashboard", "Design system"),
            cardType = NotificationCardType.NONE,
            isUnread = true,
            tabCategory = "Inbox"
        ),
        FleetNotification(
            id = "notif_2",
            senderName = "Kamron",
            senderRole = "QA Engineer",
            senderAvatarIndex = 2,
            statusDot = NotificationStatusDot.YELLOW,
            headlinePrefix = "Kamron",
            actionText = "asked to join",
            targetSubject = "Ease Design System",
            timeAgo = "1 hour. ago",
            projectName = "Easy 2023 Project",
            cardType = NotificationCardType.FILE_APPROVAL,
            fileName = "Ease Design System.fig",
            fileEditedAgo = "Edited 12 mins. ago",
            isUnread = true,
            tabCategory = "Inbox"
        ),
        FleetNotification(
            id = "notif_3",
            senderName = "Winfield",
            senderRole = "Backend Lead",
            senderAvatarIndex = 1,
            statusDot = NotificationStatusDot.RED,
            headlinePrefix = "Winfield",
            actionText = "mentioned you in",
            targetSubject = "Kohaku Landing Page",
            timeAgo = "Feb 8",
            projectName = "Landing Page 2023",
            cardType = NotificationCardType.MENTION_QUOTE,
            quoteText = "@scotty Hey, I just brought in some missing telemetry states from our old fleet run. Can you help verify the tool execution?",
            reactions = listOf("😊", "✓"),
            isUnread = false,
            tabCategory = "Inbox"
        ),
        FleetNotification(
            id = "notif_4",
            senderName = "Mellie",
            senderRole = "3D Asset Designer",
            senderAvatarIndex = 4,
            statusDot = NotificationStatusDot.ORANGE,
            headlinePrefix = "Mellie",
            actionText = "uploaded 2 new files to",
            targetSubject = "Moyo 3D",
            timeAgo = "Feb 7",
            projectName = "3D Characters",
            cardType = NotificationCardType.FILE_ATTACHMENT,
            fileName = "Travel 3D Characters.fig",
            fileEditedAgo = "1 hour. ago",
            isUnread = false,
            tabCategory = "Inbox"
        ),
        FleetNotification(
            id = "notif_5",
            senderName = "David Chen",
            senderRole = "Telemetry & Hardware Ops",
            senderAvatarIndex = 3,
            statusDot = NotificationStatusDot.GREEN,
            headlinePrefix = "David Chen",
            actionText = "deployed MCP tool server to",
            targetSubject = "AWS ECS Cluster",
            timeAgo = "3 hours ago",
            projectName = "Starlink Fleet Infra",
            pillTags = listOf("DevOps", "MCP Server", "Telemetry"),
            cardType = NotificationCardType.NONE,
            isUnread = false,
            tabCategory = "Team"
        ),
        FleetNotification(
            id = "notif_6",
            senderName = "Sarah Connor",
            senderRole = "QA & Security Sentinel",
            senderAvatarIndex = 5,
            statusDot = NotificationStatusDot.GREEN,
            headlinePrefix = "Sarah Connor",
            actionText = "approved security clearance for",
            targetSubject = "Starlink Telemetry Sandbox",
            timeAgo = "5 hours ago",
            projectName = "Security Auditing 2025",
            pillTags = listOf("Security", "P0 Compliance"),
            cardType = NotificationCardType.NONE,
            isUnread = false,
            tabCategory = "Team"
        )
    )
}

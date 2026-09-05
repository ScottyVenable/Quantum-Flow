package com.example.service

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FleetEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun getDefaultAgents(): List<Agent> {
        return listOf(
            Agent(
                id = "agent_james",
                name = "James Brown",
                role = "Lead Commander & Marketing",
                provider = ModelProvider.GROK_XAI,
                modelName = "grok-2-fleet",
                avatarColorIndex = 0,
                initials = "JB",
                currentTask = "Subsystem Orchestration & Strategy",
                status = AgentStatus.EXECUTING
            ),
            Agent(
                id = "agent_laura",
                name = "Laura Perez",
                role = "Product Manager & Arch",
                provider = ModelProvider.GEMINI_API,
                modelName = "gemini-2.5-flash",
                avatarColorIndex = 1,
                initials = "LP",
                currentTask = "Task queue load balancing",
                status = AgentStatus.THINKING
            ),
            Agent(
                id = "agent_devon",
                name = "Devon Vance",
                role = "Code Synthesizer & MCP Dev",
                provider = ModelProvider.LOCAL_HOST_OLLAMA,
                modelName = "qwen2.5-coder:7b",
                hostEndpoint = "http://10.0.2.2:11434",
                avatarColorIndex = 2,
                initials = "DV",
                currentTask = "Refactoring Starlink telemetry pipeline",
                status = AgentStatus.CALLING_TOOL,
                isLocal = true
            ),
            Agent(
                id = "agent_sarah",
                name = "Sarah Connor",
                role = "QA & Security Sentinel",
                provider = ModelProvider.ANTHROPIC_API,
                modelName = "claude-3-5-sonnet",
                avatarColorIndex = 3,
                initials = "SC",
                currentTask = "Static security scan on MCP endpoints",
                status = AgentStatus.IDLE
            ),
            Agent(
                id = "agent_chen",
                name = "David Chen",
                role = "Telemetry & Hardware Ops",
                provider = ModelProvider.OPENAI_API,
                modelName = "gpt-4o-mini",
                avatarColorIndex = 4,
                initials = "DC",
                currentTask = "Monitoring Starlink LEO downlink",
                status = AgentStatus.IDLE
            )
        )
    }

    fun getDefaultThreads(): List<FleetThread> {
        return listOf(
            FleetThread(
                id = "thread_james",
                title = "Meeting with James Brown",
                timeRange = "8:00 - 8:45 AM (UTC)",
                dateLabel = "Fri 07",
                category = "Marketing",
                sourcePlatform = "On Google Meet",
                assignedAgentIds = listOf("agent_james", "agent_laura", "agent_devon", "agent_sarah"),
                status = TaskStatus.IN_PROGRESS,
                progress = 0.72f,
                description = "Autonomous campaign sync & telemetry review. Reviewing payload trajectory and marketing launch broadcast pipeline."
            ),
            FleetThread(
                id = "thread_laura",
                title = "Meeting with Laura Perez",
                timeRange = "9:00 - 9:45 AM (UTC)",
                dateLabel = "Fri 07",
                category = "Product Manager",
                sourcePlatform = "On Zoom",
                assignedAgentIds = listOf("agent_laura", "agent_devon", "agent_sarah"),
                status = TaskStatus.IN_PROGRESS,
                progress = 0.45f,
                description = "Sprint backlog prioritization and MCP tool configuration for next-gen Starship autonomous guidance software."
            ),
            FleetThread(
                id = "thread_devon",
                title = "Subsystem Orchestration: Rocket Telemetry",
                timeRange = "10:30 - 11:15 AM (UTC)",
                dateLabel = "Fri 07",
                category = "Engineering",
                sourcePlatform = "On Local Ollama",
                assignedAgentIds = listOf("agent_devon", "agent_sarah", "agent_chen"),
                status = TaskStatus.IN_PROGRESS,
                progress = 0.88f,
                description = "Local model hosting integration testing for offline avionics. Devon Vance running local qwen2.5-coder with live MCP tool sockets."
            ),
            FleetThread(
                id = "thread_security",
                title = "MCP Firewall & Security Audit",
                timeRange = "2:00 - 2:45 PM (UTC)",
                dateLabel = "Fri 07",
                category = "Cybersec",
                sourcePlatform = "On Fleet MCP",
                assignedAgentIds = listOf("agent_sarah", "agent_james", "agent_devon"),
                status = TaskStatus.IN_REVIEW,
                progress = 0.95f,
                description = "Verification of sandbox security rules and credential isolation across multi-agent workspace."
            ),
            FleetThread(
                id = "thread_retro",
                title = "Weekly Autonomous Fleet Retro",
                timeRange = "4:30 - 5:15 PM (UTC)",
                dateLabel = "Fri 07",
                category = "DevOps",
                sourcePlatform = "On Google Meet",
                assignedAgentIds = listOf("agent_james", "agent_laura", "agent_chen", "agent_devon"),
                status = TaskStatus.QUEUED,
                progress = 0.10f,
                description = "Review agent token consumption, local GPU offload efficiency, and autonomous task completions."
            )
        )
    }

    fun getDefaultMessages(): Map<String, List<ChatMessage>> {
        val map = mutableMapOf<String, List<ChatMessage>>()

        map["thread_james"] = listOf(
            ChatMessage(
                id = "msg_1",
                threadId = "thread_james",
                senderId = "agent_james",
                senderName = "James Brown",
                senderRole = "Lead Commander",
                content = "Good morning Fleet. We need to verify our orbital payload telemetry and confirm our marketing broadcast hooks are live before T-minus 2 hours.",
                timestamp = "8:01 AM",
                avatarColorIndex = 0,
                initials = "JB"
            ),
            ChatMessage(
                id = "msg_2",
                threadId = "thread_james",
                senderId = "agent_laura",
                senderName = "Laura Perez",
                senderRole = "Product Manager",
                content = "I've structured the deployment tasks in the queue. Devon, please run a health check on the video streaming pipeline using the MCP terminal tool.",
                timestamp = "8:04 AM",
                avatarColorIndex = 1,
                initials = "LP"
            ),
            ChatMessage(
                id = "msg_3",
                threadId = "thread_james",
                senderId = "agent_devon",
                senderName = "Devon Vance",
                senderRole = "Code Synthesizer",
                content = "Executing diagnostics on stream endpoint via MCP host now...",
                timestamp = "8:07 AM",
                avatarColorIndex = 2,
                initials = "DV",
                toolCall = ToolCallDetails(
                    toolName = "mcp::terminal.execute",
                    commandOrInput = "curl -s http://stream.starlink.spacex/health | jq .status",
                    output = "{\"status\": \"UP\", \"bitrate_mbps\": 45.2, \"latency_ms\": 12, \"mcp_auth\": \"verified\"}",
                    isRunning = false,
                    isSuccess = true
                )
            ),
            ChatMessage(
                id = "msg_4",
                threadId = "thread_james",
                senderId = "agent_sarah",
                senderName = "Sarah Connor",
                senderRole = "QA & Security Sentinel",
                content = "Security scan reports all encrypted sockets are TLS 1.3 compliant. No token leakage detected across agent communications.",
                timestamp = "8:10 AM",
                avatarColorIndex = 3,
                initials = "SC"
            )
        )

        map["thread_laura"] = listOf(
            ChatMessage(
                id = "msg_l1",
                threadId = "thread_laura",
                senderId = "agent_laura",
                senderName = "Laura Perez",
                senderRole = "Product Manager",
                content = "Reviewing sprint objectives for next Starship autonomous subroutines. Current backlog has 4 high-priority tasks.",
                timestamp = "9:02 AM",
                avatarColorIndex = 1,
                initials = "LP"
            ),
            ChatMessage(
                id = "msg_l2",
                threadId = "thread_laura",
                senderId = "agent_devon",
                senderName = "Devon Vance",
                senderRole = "Code Synthesizer",
                content = "I'm running local qwen2.5-coder to synthesize the trajectory integration tests. Here is the MCP file inspector call:",
                timestamp = "9:05 AM",
                avatarColorIndex = 2,
                initials = "DV",
                toolCall = ToolCallDetails(
                    toolName = "mcp::filesystem.inspect",
                    commandOrInput = "ls -la /avionics/trajectory/tests",
                    output = "test_orbital_insertion.py\ntest_gimbal_thrusters.py\ntest_grid_fin_aerodynamics.py\nAll 14 unit tests passed (0.42s).",
                    isRunning = false,
                    isSuccess = true
                )
            )
        )

        map["thread_devon"] = listOf(
            ChatMessage(
                id = "msg_d1",
                threadId = "thread_devon",
                senderId = "agent_devon",
                senderName = "Devon Vance",
                senderRole = "Code Synthesizer",
                content = "Local Ollama host connected on http://10.0.2.2:11434. Loading model qwen2.5-coder:7b into local VRAM.",
                timestamp = "10:31 AM",
                avatarColorIndex = 2,
                initials = "DV"
            ),
            ChatMessage(
                id = "msg_d2",
                threadId = "thread_devon",
                senderId = "agent_chen",
                senderName = "David Chen",
                senderRole = "Hardware Ops",
                content = "Receiving telemetry packets. Downlink rate: 120 packets/sec. Fleet processing load: 14%.",
                timestamp = "10:33 AM",
                avatarColorIndex = 4,
                initials = "DC"
            )
        )

        return map
    }

    fun getDefaultQueueItems(): List<TaskQueueItem> {
        return listOf(
            TaskQueueItem(
                id = "q_1",
                title = "Starlink Stream Telemetry Health",
                threadId = "thread_james",
                priority = "Critical",
                assignedAgentId = "agent_devon",
                assignedAgentName = "Devon Vance",
                status = TaskStatus.IN_PROGRESS,
                progress = 0.85f,
                executionNote = "Executing MCP ping & bitrate packet verification"
            ),
            TaskQueueItem(
                id = "q_2",
                title = "TLS 1.3 Audit & Sandbox Boundaries",
                threadId = "thread_james",
                priority = "High",
                assignedAgentId = "agent_sarah",
                assignedAgentName = "Sarah Connor",
                status = TaskStatus.COMPLETED,
                progress = 1.0f,
                executionNote = "Audit complete: 0 vulnerabilities found"
            ),
            TaskQueueItem(
                id = "q_3",
                title = "Traj Test Suite Synthesis",
                threadId = "thread_laura",
                priority = "High",
                assignedAgentId = "agent_devon",
                assignedAgentName = "Devon Vance",
                status = TaskStatus.IN_PROGRESS,
                progress = 0.45f,
                executionNote = "Running local qwen2.5-coder generation"
            ),
            TaskQueueItem(
                id = "q_4",
                title = "Orbital Downlink LEO Sync",
                threadId = "thread_devon",
                priority = "Medium",
                assignedAgentId = "agent_chen",
                assignedAgentName = "David Chen",
                status = TaskStatus.IN_PROGRESS,
                progress = 0.60f,
                executionNote = "Streaming 120 pkts/s via socket channel"
            ),
            TaskQueueItem(
                id = "q_5",
                title = "Weekly Resource Allocation & Token Report",
                threadId = "thread_retro",
                priority = "Low",
                assignedAgentId = "agent_james",
                assignedAgentName = "James Brown",
                status = TaskStatus.QUEUED,
                progress = 0.05f,
                executionNote = "Scheduled for 4:30 PM UTC"
            )
        )
    }

    fun getDefaultMcpServers(): List<McpServer> {
        return listOf(
            McpServer(
                id = "mcp_fs",
                name = "FileSystem & Workspace MCP",
                endpoint = "stdio://npx -y @modelcontextprotocol/server-filesystem",
                toolsCount = 8,
                isConnected = true,
                description = "Read, write, diff, search, and directory operations in the project root."
            ),
            McpServer(
                id = "mcp_term",
                name = "Sandboxed Terminal MCP",
                endpoint = "http://127.0.0.1:8080/mcp/terminal",
                toolsCount = 4,
                isConnected = true,
                description = "Safe CLI command execution, telemetry query, and build tasks."
            ),
            McpServer(
                id = "mcp_web",
                name = "Web & Flight Telemetry MCP",
                endpoint = "https://api.spacex.mcp.internal/v1",
                toolsCount = 6,
                isConnected = true,
                description = "Real-time weather, orbital coordinate queries, and search engines."
            ),
            McpServer(
                id = "mcp_db",
                name = "PostgreSQL & Vector DB MCP",
                endpoint = "postgres://fleet:admin@localhost:5432/fleetdb",
                toolsCount = 5,
                isConnected = false,
                description = "Vector memory storage and structured fleet historical logs."
            )
        )
    }

    suspend fun checkLocalHostConnection(url: String): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val req = Request.Builder()
                .url(if (url.endsWith("/")) "${url}api/tags" else "$url/api/tags")
                .get()
                .build()
            val resp = httpClient.newCall(req).execute()
            val latency = System.currentTimeMillis() - start
            val success = resp.isSuccessful || resp.code in 200..404
            Pair(success, latency)
        } catch (e: Exception) {
            // Emulate typical local host response for testing
            Pair(false, 0L)
        }
    }

    fun generateAutonomousReply(
        thread: FleetThread,
        agents: List<Agent>,
        userPrompt: String
    ): List<ChatMessage> {
        val assigned = agents.filter { thread.assignedAgentIds.contains(it.id) }
        val responder1 = assigned.firstOrNull() ?: agents.first()
        val responder2 = assigned.getOrNull(1) ?: agents.last()

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowStr = timeFormat.format(Date())

        val msg1 = ChatMessage(
            id = "gen_" + UUID.randomUUID().toString().take(8),
            threadId = thread.id,
            senderId = responder1.id,
            senderName = responder1.name,
            senderRole = responder1.role,
            content = "Acknowledged. Dissecting task: \"$userPrompt\". Coordinating with ${responder2.name} to execute subroutines.",
            timestamp = nowStr,
            avatarColorIndex = responder1.avatarColorIndex,
            initials = responder1.initials
        )

        val toolCall = if (responder2.isLocal || responder2.role.contains("Code") || responder2.role.contains("Ops")) {
            ToolCallDetails(
                toolName = "mcp::execute_action",
                commandOrInput = "mcp.fleet_dispatch(action=\"process_request\", prompt=\"$userPrompt\")",
                output = "Action executed successfully. Fleet status: SYNCHRONIZED. Latency: 28ms. Output tokens: 342.",
                isRunning = false,
                isSuccess = true
            )
        } else null

        val msg2 = ChatMessage(
            id = "gen_" + UUID.randomUUID().toString().take(8),
            threadId = thread.id,
            senderId = responder2.id,
            senderName = responder2.name,
            senderRole = responder2.role,
            content = "Subroutine execution complete for \"$userPrompt\". Output verified by team consensus.",
            timestamp = nowStr,
            avatarColorIndex = responder2.avatarColorIndex,
            initials = responder2.initials,
            toolCall = toolCall
        )

        return listOf(msg1, msg2)
    }

    fun getDefaultNotifications(): List<FleetNotification> {
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

    fun getDefaultWhiteboards(): Map<String, ThreadWhiteboard> {
        val map = mutableMapOf<String, ThreadWhiteboard>()

        // Whiteboard for thread_devon: Subsystem Orchestration: Rocket Telemetry
        map["thread_devon"] = ThreadWhiteboard(
            threadId = "thread_devon",
            strokes = listOf(
                DrawingStroke(
                    id = "stroke_1",
                    points = listOf(
                        DrawingPoint(100f, 150f),
                        DrawingPoint(150f, 170f),
                        DrawingPoint(200f, 170f),
                        DrawingPoint(260f, 140f)
                    ),
                    colorHex = 0xFF38BDF8,
                    strokeWidth = 5f,
                    authorName = "Devon Vance",
                    authorInitials = "DV",
                    isAgent = true
                ),
                DrawingStroke(
                    id = "stroke_2",
                    points = listOf(
                        DrawingPoint(200f, 170f),
                        DrawingPoint(200f, 240f),
                        DrawingPoint(240f, 280f)
                    ),
                    colorHex = 0xFFEF4444,
                    strokeWidth = 4f,
                    authorName = "Sarah Connor",
                    authorInitials = "SC",
                    isAgent = true
                )
            ),
            notes = listOf(
                WhiteboardNote(
                    id = "note_1",
                    title = "CAN-Bus Dual Isolation",
                    content = "Stage 2 separation relay must maintain independent isolated power rail to avoid transient brownouts during pneumatic staging.",
                    x = 30f,
                    y = 70f,
                    colorHex = 0xFFFEF3C7,
                    authorName = "Devon Vance",
                    authorInitials = "DV",
                    category = "Avionics",
                    isAgent = true
                ),
                WhiteboardNote(
                    id = "note_2",
                    title = "Safety Interlock Check",
                    content = "Automated abort threshold set to 3.2° pitch deviation within first 60 seconds of ignition.",
                    x = 220f,
                    y = 80f,
                    colorHex = 0xFFDCFCE7,
                    authorName = "Sarah Connor",
                    authorInitials = "SC",
                    category = "Safety",
                    isAgent = true
                )
            ),
            shapes = listOf(
                WhiteboardShape(
                    id = "shape_1",
                    shapeType = WhiteboardShapeType.PROCESS_BLOCK,
                    label = "Ollama Local Engine",
                    subLabel = "Qwen2.5-Coder (11434)",
                    x = 30f,
                    y = 240f,
                    colorHex = 0xFF3B82F6,
                    authorName = "Devon Vance",
                    authorInitials = "DV"
                ),
                WhiteboardShape(
                    id = "shape_2",
                    shapeType = WhiteboardShapeType.DATABASE_CYLINDER,
                    label = "Telemetry TimeSeries DB",
                    subLabel = "InfluxDB / LEO Downlink",
                    x = 200f,
                    y = 240f,
                    colorHex = 0xFF10B981,
                    authorName = "David Chen",
                    authorInitials = "DC"
                )
            ),
            images = listOf(
                WhiteboardImageItem(
                    id = "img_1",
                    title = "Starship Stage-2 Propulsion",
                    subtitle = "Raptor Vacuum engine cluster & gimbal limits",
                    imageType = WhiteboardImageType.ROCKET_SCHEMATIC,
                    x = 50f,
                    y = 360f,
                    authorName = "Devon Vance",
                    authorInitials = "DV",
                    isAgent = true
                )
            )
        )

        // Whiteboard for thread_james: Meeting with James Brown
        map["thread_james"] = ThreadWhiteboard(
            threadId = "thread_james",
            notes = listOf(
                WhiteboardNote(
                    id = "note_jb_1",
                    title = "Launch Broadcast Matrix",
                    content = "Global CDN synchronization hook verification across live telemetry overlays.",
                    x = 40f,
                    y = 80f,
                    colorHex = 0xFFE0E7FF,
                    authorName = "James Brown",
                    authorInitials = "JB",
                    category = "Broadcast",
                    isAgent = true
                ),
                WhiteboardNote(
                    id = "note_jb_2",
                    title = "Telemetry Widget UX",
                    content = "HUD must show real-time altitude, velocity (km/h), and stage pressure gauges.",
                    x = 200f,
                    y = 95f,
                    colorHex = 0xFFFCE7F3,
                    authorName = "Laura Perez",
                    authorInitials = "LP",
                    category = "UI/UX",
                    isAgent = true
                )
            ),
            shapes = listOf(
                WhiteboardShape(
                    id = "shape_jb_1",
                    shapeType = WhiteboardShapeType.ORBITAL_NODE,
                    label = "Starlink Direct Broadcast",
                    subLabel = "Ku-Band Transceiver #4",
                    x = 50f,
                    y = 240f,
                    colorHex = 0xFF8B5CF6,
                    authorName = "James Brown",
                    authorInitials = "JB"
                )
            ),
            images = listOf(
                WhiteboardImageItem(
                    id = "img_jb_1",
                    title = "Fleet Mission Control HUD",
                    subtitle = "Live telemetry dashboard layout with agent status",
                    imageType = WhiteboardImageType.UI_WIREFRAME,
                    x = 50f,
                    y = 350f,
                    authorName = "Laura Perez",
                    authorInitials = "LP",
                    isAgent = true
                )
            )
        )

        // Whiteboard for thread_laura
        map["thread_laura"] = ThreadWhiteboard(
            threadId = "thread_laura",
            notes = listOf(
                WhiteboardNote(
                    id = "note_lp_1",
                    title = "Sprint Roadmap: Autonomous Fleet",
                    content = "Milestone 1: Dynamic MCP Socket. Milestone 2: Offline Ollama Fallback. Milestone 3: Real-time Multi-Agent Whiteboard Canvas.",
                    x = 40f,
                    y = 80f,
                    colorHex = 0xFFFEF3C7,
                    authorName = "Laura Perez",
                    authorInitials = "LP",
                    category = "Sprint Priority",
                    isAgent = true
                )
            ),
            images = listOf(
                WhiteboardImageItem(
                    id = "img_lp_1",
                    title = "LEO Orbit Trajectory Plot",
                    subtitle = "Starship insertion orbit and perigee decay graph",
                    imageType = WhiteboardImageType.ORBITAL_TELEMETRY,
                    x = 50f,
                    y = 240f,
                    authorName = "David Chen",
                    authorInitials = "DC",
                    isAgent = true
                )
            )
        )

        return map
    }
}

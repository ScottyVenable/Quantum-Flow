package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AgentEntity
import com.example.data.local.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.TaskStatus
import com.example.model.LocalHostConfig
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FleetNavTab
import com.example.ui.viewmodel.FleetViewModel

@Composable
fun FleetMainScreen(
    viewModel: FleetViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    val isLight = !uiState.isDarkMode
    val navBg = if (isLight) Color.White else Color(0xFF14161A)
    val navBorder = if (isLight) Color(0xFFE5E7EB) else Color(0xFF262930)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLight) Color(0xFFF7F8FA) else Color(0xFF0C0D10))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content area based on Bottom Nav Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.selectedNavTab) {
                    FleetNavTab.HOME -> {
                        NotewareLMHomeScreen(
                            messages = messages,
                            agents = agents,
                            isAutonomousRunning = uiState.isAutonomousRunning,
                            autonomousStep = uiState.autonomousCurrentStep,
                            isBookmarked = uiState.isBookmarked,
                            isDarkMode = uiState.isDarkMode,
                            onToggleBookmark = { viewModel.toggleBookmark() },
                            onSendMessage = { text -> viewModel.sendMessage(text, null) },
                            onOpenSidebar = { viewModel.setSidebarOpen(true) },
                            onOpenWhiteboard = { viewModel.openWhiteboard() },
                            onClearChat = { viewModel.clearChat() },
                            onOpenAddTask = { viewModel.showAddTaskDialog(true) }
                        )
                    }
                    FleetNavTab.TODO -> {
                        TodoTabView(
                            tasks = tasks,
                            agents = agents,
                            isDarkMode = uiState.isDarkMode,
                            onToggleTaskStatus = { id, status -> viewModel.updateTaskStatus(id, status) },
                            onOpenCreateTask = { viewModel.showAddTaskDialog(true) }
                        )
                    }
                    FleetNavTab.SEARCH -> {
                        SearchTabView(
                            messages = messages,
                            tasks = tasks,
                            isDarkMode = uiState.isDarkMode,
                            onOpenWhiteboard = { viewModel.openWhiteboard() }
                        )
                    }
                    FleetNavTab.SETTINGS -> {
                        SettingsView(
                            userName = uiState.userName,
                            userEmail = uiState.userEmail,
                            isDarkMode = uiState.isDarkMode,
                            onToggleDarkMode = { viewModel.setDarkMode(!uiState.isDarkMode) },
                            notificationsEnabled = uiState.notificationsEnabled,
                            onToggleNotifications = { enabled -> viewModel.setNotificationsEnabled(enabled) },
                            storageUsedMb = uiState.storageUsedMb,
                            storageTotalMb = uiState.storageTotalMb,
                            localHostConfig = LocalHostConfig(
                                endpointUrl = uiState.localHostUrl,
                                defaultModel = uiState.selectedLocalModel,
                                isConnected = uiState.localHostStatus?.isReachable == true
                            ),
                            onOpenLocalHostConfig = { viewModel.showLocalHostDialog(true) },
                            onOpenAddAgent = { viewModel.showAddAgentDialog(true) }
                        )
                    }
                }
            }

            // Bottom Navigation Bar (Matches Reference Image 4 Tabs: Home, To-do, Search, Settings)
            NotewareBottomNavBar(
                selectedTab = uiState.selectedNavTab,
                isDarkMode = uiState.isDarkMode,
                onSelectTab = { viewModel.selectNavTab(it) }
            )
        }

        // Overlay Task Queue Sidebar Drawer
        AnimatedVisibility(
            visible = uiState.isSidebarOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { viewModel.setSidebarOpen(false) }
            ) {
                Box(modifier = Modifier.clickable(enabled = false) {}) {
                    TaskQueueSidebar(
                        tasks = tasks,
                        selectedFilter = uiState.taskFilter,
                        onFilterChange = { viewModel.setTaskFilter(it) },
                        onUpdateTaskStatus = { id, status -> viewModel.updateTaskStatus(id, status) },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onOpenCreateTask = { viewModel.showAddTaskDialog(true) },
                        onCloseSidebar = { viewModel.setSidebarOpen(false) }
                    )
                }
            }
        }

        // Dialogs
        if (uiState.isAddAgentDialogOpen) {
            AgentManagementDialog(
                onAddAgent = { name, callSign, role, provider, modelName, endpointUrl, apiKey, systemPrompt, tools, color ->
                    viewModel.addNewAgent(name, callSign, role, provider, modelName, endpointUrl, apiKey, systemPrompt, tools, color)
                },
                onDismiss = { viewModel.showAddAgentDialog(false) }
            )
        }

        if (uiState.isAddTaskDialogOpen) {
            CreateTaskDialog(
                agents = agents,
                onAddTask = { title, desc, agentId, prio ->
                    viewModel.addNewTask(title, desc, agentId, prio)
                },
                onDismiss = { viewModel.showAddTaskDialog(false) }
            )
        }

        if (uiState.isLocalHostDialogOpen) {
            LocalModelSettingsDialog(
                currentUrl = uiState.localHostUrl,
                currentModel = uiState.selectedLocalModel,
                status = uiState.localHostStatus,
                isTesting = uiState.isTestingLocalHost,
                onTestConnection = { viewModel.testLocalHostConnection(it) },
                onSaveConfig = { url, model ->
                    viewModel.updateLocalHostUrl(url)
                    viewModel.updateSelectedLocalModel(model)
                },
                onDismiss = { viewModel.showLocalHostDialog(false) }
            )
        }

        // Collaborative Whiteboard Fullscreen Overlay
        AnimatedVisibility(
            visible = uiState.isWhiteboardOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            CollaborativeWhiteboardView(
                whiteboard = uiState.currentWhiteboard,
                activeAgents = agents,
                threadTitle = "Mission Alpha-Prime",
                onClose = { viewModel.closeWhiteboard() },
                onAddStroke = { viewModel.addWhiteboardStroke(it) },
                onAddNote = { viewModel.addWhiteboardNote(it) },
                onAddShape = { viewModel.addWhiteboardShape(it) },
                onAddImage = { viewModel.addWhiteboardImage(it) },
                onDeleteNote = { viewModel.deleteWhiteboardNote(it) },
                onDeleteShape = { viewModel.deleteWhiteboardShape(it) },
                onDeleteImage = { viewModel.deleteWhiteboardImage(it) },
                onClearBoard = { viewModel.clearWhiteboard() },
                onTriggerAutonomousCollaboration = { viewModel.triggerAutonomousWhiteboardCollaboration(it) },
                onPostSnapshotToChat = { viewModel.postWhiteboardSnapshotToChat() }
            )
        }
    }
}

/**
 * NotewareLM Note / Chat Screen matching the left phone in the user's reference image
 */
@Composable
fun NotewareLMHomeScreen(
    messages: List<MessageEntity>,
    agents: List<AgentEntity>,
    isAutonomousRunning: Boolean,
    autonomousStep: String,
    isBookmarked: Boolean,
    isDarkMode: Boolean,
    onToggleBookmark: () -> Unit,
    onSendMessage: (String) -> Unit,
    onOpenSidebar: () -> Unit,
    onOpenWhiteboard: () -> Unit,
    onClearChat: () -> Unit,
    onOpenAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInput by remember { mutableStateOf("") }
    var showQuickMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isLight = !isDarkMode
    val textPrimary = if (isLight) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textSecondary = if (isLight) Color(0xFF6B7280) else Color(0xFF9CA3AF)
    val buttonBg = if (isLight) Color(0xFFF3F4F6) else Color(0xFF1E2128)
    val inputBarBg = if (isLight) Color(0xFFF3F4F6) else Color(0xFF1E2128)

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Header Bar (Matches left phone in reference image)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Rounded Back Button `<`
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(buttonBg)
                    .clickable(onClick = onOpenSidebar)
                    .testTag("home_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Menu / Squad",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Title: ECO201 - Revision",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Revision Note",
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1
                )
            }

            // Right Bookmark Button (Save Note)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(buttonBg)
                    .clickable(onClick = onToggleBookmark)
                    .testTag("bookmark_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) (if (isLight) Color(0xFF111827) else Color.White) else textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Autonomous Fleet Running Banner if active
        AnimatedVisibility(visible = isAutonomousRunning) {
            Surface(
                color = if (isLight) Color(0xFFEFF6FF) else Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = if (isLight) Color(0xFF111827) else Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FLEET COLLABORATING: $autonomousStep",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }
        }

        // Notes / Chat Messages Feed
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.content.contains("[WHITEBOARD ARTIFACT]") || msg.senderRole == "COLLABORATIVE_CANVAS") {
                        NotewareWhiteboardCard(
                            message = msg,
                            isLight = isLight,
                            onOpenWhiteboard = onOpenWhiteboard
                        )
                    } else if (msg.senderType == MessageType.TOOL_EXECUTION) {
                        ToolCallCard(
                            toolName = msg.toolName ?: "mcp_tool",
                            toolInput = msg.toolInput,
                            toolOutput = msg.toolOutput,
                            agentName = msg.senderName
                        )
                    } else if (msg.senderType == MessageType.USER) {
                        // User message: Solid black container with white text
                        NotewareUserBubble(
                            text = msg.content,
                            isLight = isLight
                        )
                    } else {
                        // Agent message: Soft light gray container with dark text
                        NotewareAgentBubble(
                            agentName = msg.senderName,
                            text = msg.content,
                            isLight = isLight
                        )
                    }
                }
            }
        }

        // Quick Menu Sheet if opened via `+` button
        AnimatedVisibility(visible = showQuickMenu) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isLight) Color.White else Color(0xFF1B1E26),
                border = BorderStroke(1.dp, if (isLight) Color(0xFFE5E7EB) else Color(0xFF2A2E38)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actions & Workspaces",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        IconButton(
                            onClick = { showQuickMenu = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showQuickMenu = false
                                onOpenWhiteboard()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLight) Color(0xFF111827) else Color.White,
                                contentColor = if (isLight) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Whiteboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                showQuickMenu = false
                                onOpenAddTask()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Task", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Floating Bottom Input Bar (Matches Left Phone in Reference Image)
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = inputBarBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Circular `+` Button in white circle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color.White else Color(0xFF2C303B))
                        .clickable { showQuickMenu = !showQuickMenu }
                        .testTag("chat_add_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Tools",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Center Text Input
                BasicTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = textPrimary
                    ),
                    cursorBrush = SolidColor(textPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_note_input"),
                    decorationBox = { innerTextField ->
                        Box {
                            if (chatInput.isEmpty()) {
                                Text(
                                    text = "Write a note...",
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Right Solid Circular Send Button (Black circle with white send icon)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFF111827) else Color.White)
                        .clickable {
                            if (chatInput.isNotBlank() && !isAutonomousRunning) {
                                val text = chatInput.trim()
                                chatInput = ""
                                onSendMessage(text)
                            }
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (isLight) Color.White else Color.Black,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * User message bubble: Solid black container with pure white text and "Now ✓✓" timestamp below
 */
@Composable
private fun NotewareUserBubble(
    text: String,
    isLight: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isLight) Color(0xFF111827) else Color.White,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isLight) Color.White else Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = "Now",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = if (isLight) Color(0xFF111827) else Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * Agent message bubble: Soft light gray container with dark text and "Now" timestamp below
 */
@Composable
private fun NotewareAgentBubble(
    agentName: String,
    text: String,
    isLight: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isLight) Color(0xFFF3F4F6) else Color(0xFF21242C),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isLight) Color(0xFF111827) else Color(0xFFF9FAFB),
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "Now",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/**
 * Clean Minimalist Collaborative Whiteboard Card in NotewareLM Style
 */
@Composable
private fun NotewareWhiteboardCard(
    message: MessageEntity,
    isLight: Boolean,
    onOpenWhiteboard: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isLight) Color.White else Color(0xFF16181D),
        border = BorderStroke(1.dp, if (isLight) Color(0xFFECEEF2) else Color(0xFF252932)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWhiteboard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isLight) Color(0xFFF3F4F6) else Color(0xFF23272F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Brush,
                            contentDescription = null,
                            tint = if (isLight) Color(0xFF111827) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Collaborative Whiteboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLight) Color(0xFF111827) else Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLight) Color(0xFF166534) else Color(0xFF86EFAC),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message.content,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = if (isLight) Color(0xFF6B7280) else Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenWhiteboard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLight) Color(0xFF111827) else Color.White,
                    contentColor = if (isLight) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Whiteboard & Co-Draw", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Bottom Navigation Bar with 4 tabs matching reference screenshot: Home, To-do, Search, Settings
 */
@Composable
fun NotewareBottomNavBar(
    selectedTab: FleetNavTab,
    isDarkMode: Boolean,
    onSelectTab: (FleetNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = !isDarkMode
    val navBg = if (isLight) Color.White else Color(0xFF14161A)
    val navBorder = if (isLight) Color(0xFFECEEF2) else Color(0xFF262930)
    val activeColor = if (isLight) Color(0xFF111827) else Color.White
    val inactiveColor = Color(0xFF9CA3AF)

    Surface(
        color = navBg,
        border = BorderStroke(1.dp, navBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            NotewareBottomNavItem(
                icon = if (selectedTab == FleetNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedTab == FleetNavTab.HOME,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onSelectTab(FleetNavTab.HOME) }
            )

            // Tab 2: To-do
            NotewareBottomNavItem(
                icon = if (selectedTab == FleetNavTab.TODO) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List,
                label = "To-do",
                isSelected = selectedTab == FleetNavTab.TODO,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onSelectTab(FleetNavTab.TODO) }
            )

            // Tab 3: Search
            NotewareBottomNavItem(
                icon = if (selectedTab == FleetNavTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                label = "Search",
                isSelected = selectedTab == FleetNavTab.SEARCH,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onSelectTab(FleetNavTab.SEARCH) }
            )

            // Tab 4: Settings
            NotewareBottomNavItem(
                icon = if (selectedTab == FleetNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                label = "Settings",
                isSelected = selectedTab == FleetNavTab.SETTINGS,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onSelectTab(FleetNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NotewareBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

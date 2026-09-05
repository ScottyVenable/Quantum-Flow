package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AgentEntity
import com.example.data.local.TaskEntity
import com.example.data.model.TaskStatus

@Composable
fun TodoTabView(
    tasks: List<TaskEntity>,
    agents: List<AgentEntity>,
    isDarkMode: Boolean,
    onToggleTaskStatus: (String, TaskStatus) -> Unit,
    onOpenCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val isLight = !isDarkMode
    val cardBackground = if (isLight) Color.White else Color(0xFF16181D)
    val cardBorder = if (isLight) Color(0xFFECEEF2) else Color(0xFF252932)
    val textPrimary = if (isLight) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textSecondary = if (isLight) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    val filteredTasks = when (selectedFilter) {
        "Pending" -> tasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.FAILED }
        "Completed" -> tasks.filter { it.status == TaskStatus.COMPLETED }
        else -> tasks
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLight) Color(0xFFF7F8FA) else Color(0xFF0C0D10))
            .statusBarsPadding(),
        containerColor = if (isLight) Color(0xFFF7F8FA) else Color(0xFF0C0D10),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreateTask,
                containerColor = if (isLight) Color(0xFF111827) else Color.White,
                contentColor = if (isLight) Color.White else Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("add_todo_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "To-do",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "${tasks.count { it.status == TaskStatus.COMPLETED }}/${tasks.size} tasks completed",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            // Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Pending", "Completed")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            if (isLight) Color(0xFF111827) else Color.White
                        } else {
                            if (isLight) Color(0xFFE5E7EB) else Color(0xFF262930)
                        },
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isLight) Color.White else Color.Black
                            } else textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tasks found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add a new task to the queue",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val isCompleted = task.status == TaskStatus.COMPLETED
                        val assignedAgent = agents.find { it.id == task.assignedAgentId }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = cardBackground,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nextStatus = if (isCompleted) TaskStatus.QUEUED else TaskStatus.COMPLETED
                                    onToggleTaskStatus(task.id, nextStatus)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox Circle
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) {
                                                if (isLight) Color(0xFF111827) else Color.White
                                            } else Color.Transparent
                                        )
                                        .then(
                                            if (!isCompleted) Modifier.background(
                                                color = Color.Transparent,
                                                shape = CircleShape
                                            ) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = if (isLight) Color.White else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Pending",
                                            tint = textSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCompleted) textSecondary else textPrimary,
                                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    if (task.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = task.description,
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Priority Tag
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isLight) Color(0xFFF3F4F6) else Color(0xFF262930)
                                        ) {
                                            Text(
                                                text = task.priority.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Assigned Agent Tag
                                        if (assignedAgent != null) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isLight) Color(0xFFF3F4F6) else Color(0xFF262930)
                                            ) {
                                                Text(
                                                    text = assignedAgent.name,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

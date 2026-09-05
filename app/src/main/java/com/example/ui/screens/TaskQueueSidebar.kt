package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SpaceCardDark
import com.example.ui.theme.SpaceElevatedDark
import com.example.ui.theme.SpaceSurfaceDark
import com.example.ui.theme.TelemetryGreen

@Composable
fun TaskQueueSidebar(
    tasks: List<TaskEntity>,
    selectedFilter: TaskStatus?,
    onFilterChange: (TaskStatus?) -> Unit,
    onUpdateTaskStatus: (String, TaskStatus) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenCreateTask: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTasks = when (selectedFilter) {
        null -> tasks
        else -> tasks.filter { it.status == selectedFilter }
    }

    val queuedCount = tasks.count { it.status == TaskStatus.QUEUED }
    val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.MCP_EXEC }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(SpaceSurfaceDark)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(14.dp)
            .testTag("task_queue_sidebar")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Task Queues",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TASK QUEUES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onCloseSidebar,
                modifier = Modifier.size(28.dp).testTag("close_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Sidebar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time Fleet Queue Telemetry HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SpaceElevatedDark)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QueueMetricItem(label = "TOTAL", count = tasks.size, color = Color.White)
            QueueMetricItem(label = "RUNNING", count = inProgressCount, color = CyberCyan)
            QueueMetricItem(label = "QUEUED", count = queuedCount, color = Color(0xFFFFB300))
            QueueMetricItem(label = "DONE", count = completedCount, color = TelemetryGreen)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterPill(
                    label = "ALL",
                    isSelected = selectedFilter == null,
                    onClick = { onFilterChange(null) }
                )
            }
            item {
                FilterPill(
                    label = "RUNNING",
                    isSelected = selectedFilter == TaskStatus.IN_PROGRESS,
                    onClick = { onFilterChange(TaskStatus.IN_PROGRESS) }
                )
            }
            item {
                FilterPill(
                    label = "QUEUED",
                    isSelected = selectedFilter == TaskStatus.QUEUED,
                    onClick = { onFilterChange(TaskStatus.QUEUED) }
                )
            }
            item {
                FilterPill(
                    label = "DONE",
                    isSelected = selectedFilter == TaskStatus.COMPLETED,
                    onClick = { onFilterChange(TaskStatus.COMPLETED) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Task List
        Box(modifier = Modifier.weight(1f)) {
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[QUEUE EMPTY]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tasks currently in this queue state.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onUpdateStatus = { newStatus -> onUpdateTaskStatus(task.id, newStatus) },
                            onDeleteTask = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dispatch Task Button
        Button(
            onClick = onOpenCreateTask,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dispatch_new_task_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DISPATCH TASK TO FLEET",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun QueueMetricItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else SpaceCardDark)
            .border(
                1.dp,
                if (isSelected) CyberCyan else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

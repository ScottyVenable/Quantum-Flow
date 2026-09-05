package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SpaceCardDark
import com.example.ui.theme.SpaceElevatedDark
import com.example.ui.theme.TelemetryGreen
import org.json.JSONArray

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onUpdateStatus: (TaskStatus) -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val priorityColor = Color(task.priority.colorHex)
    val statusColor = Color(task.status.colorHex)

    // Parse sub-steps from JSON array
    val subSteps = remember(task.subStepsJson) {
        try {
            val arr = JSONArray(task.subStepsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SpaceCardDark)
            .border(
                1.dp,
                if (task.status == TaskStatus.IN_PROGRESS || task.status == TaskStatus.MCP_EXEC)
                    CyberCyan.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(12.dp)
            .testTag("task_item_${task.id}")
    ) {
        // Top row: Priority badge + Status chip + Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Priority pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .border(1.dp, priorityColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.priority.label,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = priorityColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.status.label,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = statusColor
                    )
                }
            }

            // More Menu for Quick Status Switch / Delete
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Task Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SpaceElevatedDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Mark Queued", color = Color.White) },
                        onClick = {
                            onUpdateStatus(TaskStatus.QUEUED)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Set In Progress", color = CyberCyan) },
                        onClick = {
                            onUpdateStatus(TaskStatus.IN_PROGRESS)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Set Completed", color = TelemetryGreen) },
                        onClick = {
                            onUpdateStatus(TaskStatus.COMPLETED)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Task", color = Color(0xFFFF3D71)) },
                        onClick = {
                            onDeleteTask()
                            showMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = task.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Description
        if (task.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = task.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Assigned agent + Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AGENT: ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.assignedAgentName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }

            Text(
                text = "${task.progressPercent}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (task.progressPercent == 100) TelemetryGreen else CyberCyan
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { task.progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (task.progressPercent == 100) TelemetryGreen else CyberCyan,
            trackColor = SpaceElevatedDark,
        )

        // Expandable Sub-steps checklist
        AnimatedVisibility(visible = isExpanded && subSteps.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SpaceElevatedDark)
                    .padding(8.dp)
            ) {
                Text(
                    text = "SUB-STEP EXECUTION PIPELINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                subSteps.forEachIndexed { index, step ->
                    val isStepDone = when {
                        task.status == TaskStatus.COMPLETED -> true
                        task.progressPercent >= (index + 1) * (100 / subSteps.size) -> true
                        else -> false
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isStepDone) TelemetryGreen.copy(alpha = 0.2f) else Color(0xFF0F172A))
                                .border(1.dp, if (isStepDone) TelemetryGreen else Color(0xFF334155), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isStepDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = TelemetryGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = step,
                            fontSize = 10.sp,
                            color = if (isStepDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

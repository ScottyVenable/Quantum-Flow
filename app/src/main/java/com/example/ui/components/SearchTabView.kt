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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.data.local.TaskEntity

@Composable
fun SearchTabView(
    messages: List<MessageEntity>,
    tasks: List<TaskEntity>,
    isDarkMode: Boolean,
    onOpenWhiteboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val isLight = !isDarkMode
    val cardBackground = if (isLight) Color.White else Color(0xFF16181D)
    val cardBorder = if (isLight) Color(0xFFECEEF2) else Color(0xFF252932)
    val textPrimary = if (isLight) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textSecondary = if (isLight) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    val filteredMessages = remember(query, selectedCategory, messages) {
        if (selectedCategory == "Tasks") return@remember emptyList<MessageEntity>()
        if (query.isBlank()) messages.take(10)
        else messages.filter { it.content.contains(query, ignoreCase = true) || it.senderName.contains(query, ignoreCase = true) }
    }

    val filteredTasks = remember(query, selectedCategory, tasks) {
        if (selectedCategory == "Notes") return@remember emptyList<TaskEntity>()
        if (query.isBlank()) tasks.take(5)
        else tasks.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLight) Color(0xFFF7F8FA) else Color(0xFF0C0D10))
            .statusBarsPadding()
    ) {
        // Large Bold Title
        Text(
            text = "Search",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
        )

        // Search Input Bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isLight) Color(0xFFE5E7EB).copy(alpha = 0.6f) else Color(0xFF21242C),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "Search notes, tasks, agents...",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("All", "Notes", "Tasks", "Whiteboard")
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) {
                        if (isLight) Color(0xFF111827) else Color.White
                    } else {
                        if (isLight) Color(0xFFE5E7EB) else Color(0xFF262930)
                    },
                    modifier = Modifier.clickable {
                        if (cat == "Whiteboard") {
                            onOpenWhiteboard()
                        } else {
                            selectedCategory = cat
                        }
                    }
                ) {
                    Text(
                        text = cat,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (filteredMessages.isEmpty() && filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isBlank()) "Type keywords to search fleet memory" else "No matching results found",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // Tasks Section
            if (filteredTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "TASKS (${filteredTasks.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(filteredTasks, key = { "task_${it.id}" }) { task ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = task.status.name,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Notes Section
            if (filteredMessages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NOTES & DIRECTIVES (${filteredMessages.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(filteredMessages, key = { "msg_${it.id}" }) { msg ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBackground,
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${msg.senderRole}",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    color = textPrimary,
                                    lineHeight = 18.sp,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

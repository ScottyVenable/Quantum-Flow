package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SpaceBorderDark
import com.example.ui.theme.SpaceCardDark
import com.example.ui.theme.TelemetryGreen

@Composable
fun FleetHeader(
    activeAgentCount: Int,
    activeTaskCount: Int,
    isLocalHostConnected: Boolean,
    isDarkMode: Boolean,
    onToggleSidebar: () -> Unit,
    onOpenLocalHostSettings: () -> Unit,
    onOpenAddAgent: () -> Unit,
    onToggleTheme: () -> Unit,
    onClearChat: () -> Unit,
    onOpenWhiteboard: () -> Unit = {},
    whiteboardItemCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Sidebar Toggle + Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleSidebar,
                    modifier = Modifier.testTag("toggle_sidebar_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (activeTaskCount > 0) {
                                Badge(
                                    containerColor = CyberCyan,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = "$activeTaskCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Task Queue Sidebar",
                            tint = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GROK FLEET",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v2.6-ORBITAL",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Autonomous Multi-Agent Command",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Collaborative Whiteboard button
                IconButton(
                    onClick = onOpenWhiteboard,
                    modifier = Modifier.testTag("whiteboard_header_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (whiteboardItemCount > 0) {
                                Badge(
                                    containerColor = CyberCyan,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = "$whiteboardItemCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "Collaborative Whiteboard",
                            tint = CyberCyan
                        )
                    }
                }

                // Local Model Host Diagnostics button
                IconButton(
                    onClick = onOpenLocalHostSettings,
                    modifier = Modifier.testTag("local_host_button")
                ) {
                    BadgedBox(
                        badge = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLocalHostConnected) TelemetryGreen else CyberCyan)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Local Model Host",
                            tint = if (isLocalHostConnected) TelemetryGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Add Agent button
                IconButton(
                    onClick = onOpenAddAgent,
                    modifier = Modifier.testTag("add_agent_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Custom Agent",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Theme switch
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Telemetry status strip (SpaceX / Grokbot monospace HUD)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(TelemetryGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SQUAD // $activeAgentCount ONLINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TelemetryGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "MCP TOOLS: 6 ACTIVE",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = CyberCyan
            )

            Text(
                text = "QUEUE: $activeTaskCount",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalHostConfig
import com.example.ui.theme.*

@Composable
fun SettingsView(
    userName: String,
    userEmail: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    storageUsedMb: Int,
    storageTotalMb: Int,
    localHostConfig: LocalHostConfig,
    onOpenLocalHostConfig: () -> Unit,
    onOpenAddAgent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val isLight = !isDarkMode
    val cardBackground = if (isLight) Color.White else Color(0xFF16181D)
    val cardBorder = if (isLight) Color(0xFFECEEF2) else Color(0xFF252932)
    val textPrimary = if (isLight) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textSecondary = if (isLight) Color(0xFF6B7280) else Color(0xFF9CA3AF)
    val iconTint = if (isLight) Color(0xFF111827) else Color(0xFFE5E7EB)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLight) Color(0xFFF7F8FA) else Color(0xFF0C0D10))
            .statusBarsPadding()
    ) {
        // Large Bold Settings Header
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("settings_screen_title")
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: User Profile Card (Matches Reference Image)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar box with soft yellow/warm illustration style
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF08A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌻",
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // Card 2: Preferences (Dark Mode & Notifications)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Dark Mode Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = "Dark Mode",
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Dark Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isLight) Color(0xFF111827) else Color.White,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB),
                                uncheckedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    // Notifications Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Notifications",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isLight) Color(0xFF111827) else Color.White,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB),
                                uncheckedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }
                }
            }

            // Card 3: Storage (Matches Reference Progress Bar)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storage,
                            contentDescription = "Storage",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Storage",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean progress bar matching the image (solid black bar on light gray track)
                    val progressFraction = (storageUsedMb.toFloat() / storageTotalMb.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = if (isLight) Color(0xFF111827) else Color.White,
                        trackColor = if (isLight) Color(0xFFE5E7EB) else Color(0xFF2A2E38),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val percentUsed = (progressFraction * 100).toInt()
                    Text(
                        text = "$percentUsed% used — ${storageUsedMb}MB of ${storageTotalMb / 1000}GB",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }

            // Card 4: Local Model Hosting & Agent Fleet
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenLocalHostConfig)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Terminal,
                                contentDescription = "Local Ollama Host",
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Local Model Endpoint",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (localHostConfig.isConnected) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = if (localHostConfig.isConnected) "ONLINE" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (localHostConfig.isConnected) Color(0xFF166534) else Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${localHostConfig.endpointUrl} (${localHostConfig.defaultModel})",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }

            // Card 5: Actions & About (Matches Reference Image)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // About NotewareLM Row
                    SettingsNavigationRow(
                        icon = Icons.Outlined.Info,
                        title = "About NotewareLM",
                        iconTint = iconTint,
                        textColor = textPrimary,
                        onClick = { showAboutDialog = true }
                    )

                    // Privacy Policy Row
                    SettingsNavigationRow(
                        icon = Icons.Outlined.Shield,
                        title = "Privacy Policy",
                        iconTint = iconTint,
                        textColor = textPrimary,
                        onClick = { showPrivacyDialog = true }
                    )

                    // Logout / Clear Row (Soft red color as in reference screenshot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenAddAgent)
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Logout",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = textPrimary)
                }
            },
            title = { Text("About NotewareLM", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "NotewareLM Autonomous Agent Fleet & Collaborative Workspace.\n\n" +
                    "Empowers mission squads with local LLM execution, MCP tools, and multi-agent coordination with real-time vector whiteboard syncing.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = textPrimary)
                }
            },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All notes, directives, and autonomous agent executions remain strictly on your local device and private endpoints. Local Ollama & LM Studio requests do not leave your infrastructure.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

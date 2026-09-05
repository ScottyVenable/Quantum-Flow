package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.McpRegistry
import com.example.data.model.ModelProvider
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SpaceCardDark
import com.example.ui.theme.SpaceElevatedDark
import com.example.ui.theme.SpaceSurfaceDark
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen

@Composable
fun AgentManagementDialog(
    onAddAgent: (
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
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var callSign by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(ModelProvider.GEMINI) }
    var modelName by remember { mutableStateOf("gemini-3.5-flash") }
    var endpointUrl by remember { mutableStateOf(ModelProvider.GEMINI.defaultEndpoint) }
    var apiKey by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("You are an autonomous AI fleet specialist working with other agents to accomplish mission goals.") }
    val selectedTools = remember { mutableStateListOf("mcp_web_search", "mcp_code_sandbox") }
    var selectedColorHex by remember { mutableStateOf(0xFF00F0FF) }

    val colorOptions = listOf(
        0xFF00F0FF to "Cyber Cyan",
        0xFF2979FF to "Electric Blue",
        0xFFFF9100 to "Amber Engine",
        0xFF00E676 to "Telemetry Emerald",
        0xFF7C4DFF to "Quantum Violet",
        0xFFFF3D71 to "Laser Coral"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SpaceSurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .testTag("add_custom_agent_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMISSION FLEET AGENT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name & CallSign
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Agent Name (e.g. Orion)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("agent_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = callSign,
                        onValueChange = { callSign = it.uppercase() },
                        label = { Text("Call Sign", fontSize = 11.sp) },
                        modifier = Modifier.weight(0.7f).testTag("agent_callsign_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Role
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Fleet Role (e.g. Avionics Security Specialist)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("agent_role_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Model Provider Selector
                Text(
                    text = "CONNECTION PROVIDER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ModelProvider.values()) { prov ->
                        val isSel = provider == prov
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyberCyan.copy(alpha = 0.2f) else SpaceCardDark)
                                .border(1.dp, if (isSel) CyberCyan else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable {
                                    provider = prov
                                    endpointUrl = prov.defaultEndpoint
                                    modelName = when (prov) {
                                        ModelProvider.GEMINI -> "gemini-3.5-flash"
                                        ModelProvider.LOCAL_HOST -> "llama3.2"
                                        ModelProvider.OPENAI_COMPATIBLE -> "gpt-4o"
                                        ModelProvider.CUSTOM_REST -> "custom-model"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prov.displayName,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (isSel) CyberCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Model Name & Endpoint
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Model Tag", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = endpointUrl,
                        onValueChange = { endpointUrl = it },
                        label = { Text("Endpoint URL", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional API Key
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional / Uses configured secrets if blank)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("agent_api_key_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // MCP Tools Access Selection
                Text(
                    text = "MCP ACCESS & CAPABILITIES",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TelemetryAmber
                )
                Spacer(modifier = Modifier.height(4.dp))

                McpRegistry.AVAILABLE_TOOLS.forEach { tool ->
                    val isChecked = selectedTools.contains(tool.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (isChecked) selectedTools.remove(tool.id)
                                else selectedTools.add(tool.id)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) selectedTools.add(tool.id)
                                else selectedTools.remove(tool.id)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TelemetryAmber,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tool.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tool.description,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // System Prompt
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("Agent System Directives", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badge Color Picker
                Text(
                    text = "TELEMETRY COLOR BADGE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { (cHex, _) ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(cHex))
                                .border(
                                    2.dp,
                                    if (selectedColorHex == cHex) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = cHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorHex == cHex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Button
                Button(
                    onClick = {
                        onAddAgent(
                            name,
                            callSign,
                            role,
                            provider,
                            modelName,
                            endpointUrl,
                            apiKey,
                            systemPrompt,
                            selectedTools.toList(),
                            selectedColorHex
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("commission_agent_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "COMMISSION & SYNC AGENT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

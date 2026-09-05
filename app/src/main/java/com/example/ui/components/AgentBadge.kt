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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Icon
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
import com.example.data.local.AgentEntity
import com.example.data.model.AgentStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SpaceElevatedDark
import com.example.ui.theme.TelemetryGreen

@Composable
fun AgentSquadBar(
    agents: List<AgentEntity>,
    selectedAgentId: String?,
    onSelectAgent: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "ALL SQUAD" chip
        item {
            val isAllSelected = selectedAgentId == null || selectedAgentId == "fleet_all"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isAllSelected) CyberCyan.copy(alpha = 0.2f) else SpaceElevatedDark)
                    .border(
                        1.dp,
                        if (isAllSelected) CyberCyan else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectAgent(null) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("agent_chip_all")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "All Fleet",
                        tint = if (isAllSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ALL FLEET",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isAllSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        items(agents) { agent ->
            val isSelected = selectedAgentId == agent.id
            val agentColor = Color(agent.colorHex)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) agentColor.copy(alpha = 0.2f) else SpaceElevatedDark)
                    .border(
                        1.dp,
                        if (isSelected) agentColor else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectAgent(agent.id) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("agent_chip_${agent.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status indicator dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (agent.status) {
                                    AgentStatus.IDLE -> TelemetryGreen
                                    AgentStatus.THINKING -> CyberCyan
                                    AgentStatus.EXECUTING_TOOL -> Color(0xFFFFB300)
                                    AgentStatus.REVIEWING -> Color(0xFF7C4DFF)
                                    AgentStatus.COMPLETED -> TelemetryGreen
                                    AgentStatus.OFFLINE -> Color(0xFF64748B)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = agent.callSign,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) agentColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (agent.status != AgentStatus.IDLE) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${agent.status.label})",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = agentColor
                        )
                    }
                }
            }
        }
    }
}

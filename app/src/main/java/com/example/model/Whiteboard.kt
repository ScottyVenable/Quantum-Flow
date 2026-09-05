package com.example.model

import androidx.compose.ui.geometry.Offset
import java.util.UUID

enum class WhiteboardTool(val displayName: String) {
    PEN("Pen"),
    HIGHLIGHTER("Highlighter"),
    STICKY_NOTE("Sticky Note"),
    SHAPE("Architecture Shape"),
    IMAGE_SCHEMATIC("Schematic / Image"),
    ERASER("Eraser")
}

enum class WhiteboardShapeType(val label: String) {
    PROCESS_BLOCK("Service / Process"),
    DATABASE_CYLINDER("Telemetry / DB"),
    DECISION_DIAMOND("Safety Gate"),
    ORBITAL_NODE("Orbital Relay")
}

enum class WhiteboardImageType(val title: String, val badge: String) {
    ROCKET_SCHEMATIC("Starship Stage-2 Propulsion", "Avionics"),
    SYSTEM_ARCHITECTURE("Distributed Telemetry Mesh", "Architecture"),
    ORBITAL_TELEMETRY("LEO Orbit Trajectory Plot", "Flight Dynamics"),
    UI_WIREFRAME("Fleet Mission Control HUD", "Interface"),
    CUSTOM_UPLOAD("Custom Uploaded Asset", "User Asset")
}

data class DrawingPoint(
    val x: Float,
    val y: Float
)

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<DrawingPoint>,
    val colorHex: Long = 0xFF2563EB, // Primary blue
    val strokeWidth: Float = 4f,
    val isHighlighter: Boolean = false,
    val authorId: String = "user",
    val authorName: String = "Commander",
    val authorInitials: String = "YOU",
    val authorColorHex: Long = 0xFF2563EB,
    val isAgent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class WhiteboardNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val x: Float,
    val y: Float,
    val colorHex: Long = 0xFFFEF3C7, // Soft warm amber sticky note
    val authorId: String = "user",
    val authorName: String = "Commander",
    val authorInitials: String = "YOU",
    val category: String = "Planning",
    val isAgent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class WhiteboardShape(
    val id: String = UUID.randomUUID().toString(),
    val shapeType: WhiteboardShapeType,
    val label: String,
    val subLabel: String = "",
    val x: Float,
    val y: Float,
    val width: Float = 160f,
    val height: Float = 80f,
    val colorHex: Long = 0xFF3B82F6,
    val authorName: String = "Devon Vance",
    val authorInitials: String = "DV",
    val isAgent: Boolean = true
)

data class WhiteboardImageItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val imageType: WhiteboardImageType,
    val x: Float,
    val y: Float,
    val width: Float = 240f,
    val height: Float = 160f,
    val authorName: String,
    val authorInitials: String,
    val isAgent: Boolean = true,
    val customAssetUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentCursor(
    val agentId: String,
    val agentName: String,
    val initials: String,
    val role: String,
    val x: Float,
    val y: Float,
    val actionText: String, // e.g. "Drawing Avionics Bus...", "Posting safety criteria..."
    val colorHex: Long
)

data class ThreadWhiteboard(
    val threadId: String,
    val strokes: List<DrawingStroke> = emptyList(),
    val notes: List<WhiteboardNote> = emptyList(),
    val shapes: List<WhiteboardShape> = emptyList(),
    val images: List<WhiteboardImageItem> = emptyList(),
    val activeCursors: List<AgentCursor> = emptyList(),
    val isAgentCollabActive: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

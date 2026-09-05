package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AgentEntity
import com.example.model.*
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborativeWhiteboardView(
    whiteboard: ThreadWhiteboard,
    activeAgents: List<AgentEntity>,
    threadTitle: String,
    onClose: () -> Unit,
    onAddStroke: (DrawingStroke) -> Unit,
    onAddNote: (WhiteboardNote) -> Unit,
    onAddShape: (WhiteboardShape) -> Unit,
    onAddImage: (WhiteboardImageItem) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDeleteShape: (String) -> Unit,
    onDeleteImage: (String) -> Unit,
    onClearBoard: () -> Unit,
    onTriggerAutonomousCollaboration: (String) -> Unit,
    onPostSnapshotToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(WhiteboardTool.PEN) }
    var selectedColorHex by remember { mutableStateOf(0xFF00E5FF) } // CyberCyan default
    var strokeWidth by remember { mutableStateOf(4f) }

    var currentPathPoints by remember { mutableStateOf(listOf<DrawingPoint>()) }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddShapeDialog by remember { mutableStateOf(false) }
    var showAddImageDialog by remember { mutableStateOf(false) }

    val paletteColors = listOf(
        0xFF00E5FF, // CyberCyan
        0xFF3B82F6, // ElectricBlue
        0xFF10B981, // Emerald Green
        0xFFF59E0B, // Telemetry Amber
        0xFFEF4444, // Red
        0xFFA855F7, // Purple
        0xFFFFFFFF, // White
        0xFF94A3B8  // Slate Grey
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("collaborative_whiteboard_canvas")
    ) {
        // Grid background pattern on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 40.dp.toPx()
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.45f)

            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSize
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }

            // Draw completed strokes
            for (stroke in whiteboard.strokes) {
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    val strokeColor = Color(stroke.colorHex).copy(
                        alpha = if (stroke.isHighlighter) 0.35f else 1.0f
                    )
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(
                            width = if (stroke.isHighlighter) stroke.strokeWidth * 3 else stroke.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Draw current active in-progress stroke
            if (currentPathPoints.size > 1) {
                val path = Path().apply {
                    moveTo(currentPathPoints[0].x, currentPathPoints[0].y)
                    for (i in 1 until currentPathPoints.size) {
                        lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(selectedColorHex).copy(
                        alpha = if (selectedTool == WhiteboardTool.HIGHLIGHTER) 0.35f else 1f
                    ),
                    style = Stroke(
                        width = if (selectedTool == WhiteboardTool.HIGHLIGHTER) strokeWidth * 3 else strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Pointer input layer for drawing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedTool) {
                    if (selectedTool == WhiteboardTool.PEN || selectedTool == WhiteboardTool.HIGHLIGHTER) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPathPoints = listOf(DrawingPoint(offset.x, offset.y))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPathPoints = currentPathPoints + DrawingPoint(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                if (currentPathPoints.size > 1) {
                                    val newStroke = DrawingStroke(
                                        points = currentPathPoints,
                                        colorHex = selectedColorHex,
                                        strokeWidth = strokeWidth,
                                        isHighlighter = selectedTool == WhiteboardTool.HIGHLIGHTER,
                                        authorId = "user",
                                        authorName = "Commander",
                                        authorInitials = "CMD",
                                        isAgent = false
                                    )
                                    onAddStroke(newStroke)
                                }
                                currentPathPoints = emptyList()
                            },
                            onDragCancel = {
                                currentPathPoints = emptyList()
                            }
                        )
                    }
                }
        )

        // Overlay of Draggable Elements: Notes, Shapes, Schematics
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Architecture Shapes
            whiteboard.shapes.forEach { shape ->
                DraggableShapeItem(
                    shape = shape,
                    onDelete = { onDeleteShape(shape.id) }
                )
            }

            // 2. Blueprint Schematics & Images
            whiteboard.images.forEach { imageItem ->
                DraggableImageSchematicItem(
                    item = imageItem,
                    onDelete = { onDeleteImage(imageItem.id) }
                )
            }

            // 3. Sticky Notes
            whiteboard.notes.forEach { note ->
                DraggableStickyNoteItem(
                    note = note,
                    onDelete = { onDeleteNote(note.id) }
                )
            }

            // 4. Live Multi-Agent Cursors
            whiteboard.activeCursors.forEach { cursor ->
                LiveAgentCursorItem(cursor = cursor)
            }
        }

        // Top Command Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("whiteboard_back_button")
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back to Chat",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Brush,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FLEET WHITEBOARD",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${whiteboard.strokes.size + whiteboard.notes.size + whiteboard.shapes.size + whiteboard.images.size} ARTIFACTS",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }
                            }
                            Text(
                                text = "Thread: $threadTitle",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action buttons: Fleet Co-Draw & Post to Chat
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                onTriggerAutonomousCollaboration(
                                    "Coordinate orbital propulsion telemetry & failover architecture"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (whiteboard.isAgentCollabActive) TelemetryGreen else CyberCyan
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("fleet_codraw_button")
                        ) {
                            Icon(
                                if (whiteboard.isAgentCollabActive) Icons.Default.Sync else Icons.Default.GroupWork,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (whiteboard.isAgentCollabActive) "Agents Syncing..." else "Fleet Co-Draw",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = onPostSnapshotToChat,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("post_snapshot_button")
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post to Chat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Active agent presence bar
                if (whiteboard.isAgentCollabActive || whiteboard.activeCursors.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = TelemetryGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MULTIPLE AGENTS CO-AUTHORING: Falcon-9 Director, Raptor Avionics & Telemetry Ops plotting vectors simultaneously...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TelemetryGreen
                        )
                    }
                }
            }
        }

        // Bottom Tool Bar & Palettes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color palette (visible when Pen or Highlighter active)
            AnimatedVisibility(
                visible = selectedTool == WhiteboardTool.PEN || selectedTool == WhiteboardTool.HIGHLIGHTER
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paletteColors.forEach { colorHex ->
                            val isSelected = selectedColorHex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = colorHex }
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Stroke size button
                        IconButton(
                            onClick = {
                                strokeWidth = if (strokeWidth >= 8f) 2f else strokeWidth + 2f
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text(
                                text = "${strokeWidth.toInt()}px",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Main Tool Selection Pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                tonalElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pen
                    ToolButton(
                        icon = Icons.Default.Edit,
                        label = "Pen",
                        isSelected = selectedTool == WhiteboardTool.PEN,
                        onClick = { selectedTool = WhiteboardTool.PEN }
                    )

                    // Highlighter
                    ToolButton(
                        icon = Icons.Default.BorderColor,
                        label = "Highlight",
                        isSelected = selectedTool == WhiteboardTool.HIGHLIGHTER,
                        onClick = { selectedTool = WhiteboardTool.HIGHLIGHTER }
                    )

                    // Sticky Note
                    ToolButton(
                        icon = Icons.Default.StickyNote2,
                        label = "Note",
                        isSelected = false,
                        onClick = { showAddNoteDialog = true }
                    )

                    // Architecture Shape
                    ToolButton(
                        icon = Icons.Default.AccountTree,
                        label = "Shape",
                        isSelected = false,
                        onClick = { showAddShapeDialog = true }
                    )

                    // Image / Schematic
                    ToolButton(
                        icon = Icons.Default.Image,
                        label = "Schematic",
                        isSelected = false,
                        onClick = { showAddImageDialog = true }
                    )

                    // Clear Canvas
                    IconButton(
                        onClick = onClearBoard,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Clear Canvas",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog 1: Add Sticky Note
    if (showAddNoteDialog) {
        AddStickyNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content, category ->
                val newNote = WhiteboardNote(
                    title = title,
                    content = content,
                    category = category,
                    x = 80f,
                    y = 150f,
                    colorHex = when (category) {
                        "Avionics" -> 0xFFE0F2FE
                        "Safety" -> 0xFFFEE2E2
                        "Consensus" -> 0xFFDCFCE7
                        else -> 0xFFFEF3C7
                    },
                    authorName = "Commander",
                    authorInitials = "CMD",
                    isAgent = false
                )
                onAddNote(newNote)
                showAddNoteDialog = false
            }
        )
    }

    // Dialog 2: Add Architecture Shape
    if (showAddShapeDialog) {
        AddArchitectureShapeDialog(
            onDismiss = { showAddShapeDialog = false },
            onConfirm = { shapeType, label, subLabel ->
                val newShape = WhiteboardShape(
                    shapeType = shapeType,
                    label = label,
                    subLabel = subLabel,
                    x = 100f,
                    y = 200f,
                    colorHex = when (shapeType) {
                        WhiteboardShapeType.PROCESS_BLOCK -> 0xFF3B82F6
                        WhiteboardShapeType.DATABASE_CYLINDER -> 0xFF10B981
                        WhiteboardShapeType.DECISION_DIAMOND -> 0xFFF59E0B
                        WhiteboardShapeType.ORBITAL_NODE -> 0xFF8B5CF6
                    },
                    authorName = "Commander",
                    authorInitials = "CMD",
                    isAgent = false
                )
                onAddShape(newShape)
                showAddShapeDialog = false
            }
        )
    }

    // Dialog 3: Add Schematic / Blueprint Image
    if (showAddImageDialog) {
        AddSchematicDialog(
            onDismiss = { showAddImageDialog = false },
            onConfirm = { imageType, title, subtitle ->
                val newImage = WhiteboardImageItem(
                    title = title,
                    subtitle = subtitle,
                    imageType = imageType,
                    x = 60f,
                    y = 280f,
                    authorName = "Commander",
                    authorInitials = "CMD",
                    isAgent = false
                )
                onAddImage(newImage)
                showAddImageDialog = false
            }
        )
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CyberCyan.copy(alpha = 0.25f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CyberCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CyberCyan else Color(0xFFCBD5E1)
            )
        }
    }
}

// -------------------------------------------------------------
// Interactive Draggable Element 1: Sticky Note
// -------------------------------------------------------------
@Composable
fun DraggableStickyNoteItem(
    note: WhiteboardNote,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(note.x) }
    var offsetY by remember { mutableStateOf(note.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .width(180.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(note.colorHex))
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = note.category.uppercase(),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = note.content,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color.Black.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = note.authorInitials,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.authorName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }

                if (note.isAgent) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "AI AGENT",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Draggable Element 2: Architecture Shape
// -------------------------------------------------------------
@Composable
fun DraggableShapeItem(
    shape: WhiteboardShape,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(shape.x) }
    var offsetY by remember { mutableStateOf(shape.y) }

    val baseColor = Color(shape.colorHex)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .width(200.dp)
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B))
            .border(2.dp, baseColor, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (shape.shapeType) {
                            WhiteboardShapeType.PROCESS_BLOCK -> Icons.Default.Settings
                            WhiteboardShapeType.DATABASE_CYLINDER -> Icons.Default.Storage
                            WhiteboardShapeType.DECISION_DIAMOND -> Icons.Default.CheckCircle
                            WhiteboardShapeType.ORBITAL_NODE -> Icons.Default.Hub
                        },
                        contentDescription = null,
                        tint = baseColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shape.shapeType.label.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = baseColor
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = shape.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (shape.subLabel.isNotBlank()) {
                Text(
                    text = shape.subLabel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Architect: ${shape.authorName} (${shape.authorInitials})",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Draggable Element 3: Schematic Blueprint
// -------------------------------------------------------------
@Composable
fun DraggableImageSchematicItem(
    item: WhiteboardImageItem,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(item.x) }
    var offsetY by remember { mutableStateOf(item.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .width(220.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.5.dp, CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.imageType.badge.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // High-Tech Schematic Canvas Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF070B14))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    when (item.imageType) {
                        WhiteboardImageType.ROCKET_SCHEMATIC -> {
                            // Rocket nozzle / gimbal vectors
                            val midX = size.width / 2
                            drawLine(Color(0xFF38BDF8), Offset(midX, 10f), Offset(midX, 60f), 2f)
                            drawLine(Color(0xFF38BDF8), Offset(midX - 30f, 35f), Offset(midX + 30f, 35f), 2f)
                            drawLine(Color(0xFFF59E0B), Offset(midX - 25f, 60f), Offset(midX - 45f, 90f), 3f)
                            drawLine(Color(0xFFF59E0B), Offset(midX, 60f), Offset(midX, 95f), 3f)
                            drawLine(Color(0xFFF59E0B), Offset(midX + 25f, 60f), Offset(midX + 45f, 90f), 3f)
                        }
                        WhiteboardImageType.ORBITAL_TELEMETRY -> {
                            // Orbit Ellipse & Perigee
                            drawCircle(Color(0xFF3B82F6), radius = 15f, center = Offset(50f, 50f))
                            val orbitPath = Path().apply {
                                moveTo(20f, 50f)
                                quadraticTo(size.width / 2, 10f, size.width - 20f, 60f)
                                quadraticTo(size.width / 2, 90f, 20f, 50f)
                            }
                            drawPath(orbitPath, Color(0xFF00E5FF), style = Stroke(width = 2f))
                            drawCircle(Color(0xFF10B981), radius = 4f, center = Offset(size.width - 30f, 60f))
                        }
                        WhiteboardImageType.SYSTEM_ARCHITECTURE -> {
                            // Mesh Network nodes
                            val nodeA = Offset(40f, 30f)
                            val nodeB = Offset(size.width - 40f, 30f)
                            val nodeC = Offset(size.width / 2, 75f)
                            drawLine(Color(0xFF64748B), nodeA, nodeB, 1.5f)
                            drawLine(Color(0xFF64748B), nodeB, nodeC, 1.5f)
                            drawLine(Color(0xFF64748B), nodeC, nodeA, 1.5f)
                            drawCircle(CyberCyan, radius = 6f, center = nodeA)
                            drawCircle(CyberCyan, radius = 6f, center = nodeB)
                            drawCircle(CyberCyan, radius = 6f, center = nodeC)
                        }
                        else -> {
                            // UI Wireframe layout
                            drawLine(Color(0xFF334155), Offset(10f, 20f), Offset(size.width - 10f, 20f), 1f)
                            drawLine(Color(0xFF334155), Offset(50f, 20f), Offset(50f, size.height - 10f), 1f)
                            drawCircle(Color(0xFF10B981), radius = 4f, center = Offset(25f, 35f))
                            drawCircle(Color(0xFF38BDF8), radius = 4f, center = Offset(25f, 55f))
                        }
                    }
                }
            }

            // Details
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = item.subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Attached by: ${item.authorName}",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Element 4: Live Multi-Agent Cursor
// -------------------------------------------------------------
@Composable
fun LiveAgentCursorItem(cursor: AgentCursor) {
    val cursorColor = Color(cursor.colorHex)

    Box(
        modifier = Modifier
            .offset { IntOffset(cursor.x.roundToInt(), cursor.y.roundToInt()) }
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pointer arrow
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = cursorColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                // Agent Tag Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = cursorColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "${cursor.agentName} (${cursor.initials})",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Action balloon
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, cursorColor),
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            ) {
                Text(
                    text = cursor.actionText,
                    color = cursorColor,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Whiteboard Dialogs
// -------------------------------------------------------------
@Composable
fun AddStickyNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Planning") }

    val categories = listOf("Planning", "Avionics", "Safety", "Consensus")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Sticky Note to Fleet Whiteboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Strategy / Thought Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title.trim(), content.trim(), selectedCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Pin Note", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddArchitectureShapeDialog(
    onDismiss: () -> Unit,
    onConfirm: (WhiteboardShapeType, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(WhiteboardShapeType.PROCESS_BLOCK) }
    var label by remember { mutableStateOf("") }
    var subLabel by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Architecture / Logic Node",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Node Type:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WhiteboardShapeType.values()) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Component Name") },
                    placeholder = { Text("e.g. MCP Routing Gateway") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subLabel,
                    onValueChange = { subLabel = it },
                    label = { Text("Specs / Latency Metric") },
                    placeholder = { Text("e.g. Ku-Band Failover < 2ms") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (label.isNotBlank()) {
                                onConfirm(selectedType, label.trim(), subLabel.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Place Node", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSchematicDialog(
    onDismiss: () -> Unit,
    onConfirm: (WhiteboardImageType, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(WhiteboardImageType.ROCKET_SCHEMATIC) }
    var title by remember { mutableStateOf(selectedType.title) }
    var subtitle by remember { mutableStateOf("Uploaded vector technical blueprint") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Upload Technical Blueprint / Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Schematic Template:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WhiteboardImageType.values()) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                title = type.title
                            },
                            label = { Text(type.badge, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Blueprint Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(selectedType, title.trim(), subtitle.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Attach Schematic", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

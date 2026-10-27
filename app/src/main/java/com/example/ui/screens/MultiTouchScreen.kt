package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.data.model.TestStatus
import com.example.ui.components.TestTopBar
import com.example.ui.components.TestVerdictBar
import com.example.ui.theme.DiagCyan
import com.example.ui.theme.DiagGreen
import com.example.ui.theme.DiagRed
import com.example.ui.viewmodel.DiagViewModel

data class TouchPointer(
    val id: Int,
    val x: Float,
    val y: Float,
    val pressure: Float
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiTouchScreen(
    viewModel: DiagViewModel,
    onBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val status = testResults[DiagnosticTest.MULTITOUCH] ?: TestStatus.NOT_TESTED
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Points de contact, 1: Grille zones mortes

    // Active pointers for Tab 0
    val activePointers = remember { mutableStateListOf<TouchPointer>() }
    var maxPointersEver by remember { mutableIntStateOf(0) }

    // Grid test state for Tab 1 (12 rows x 8 cols = 96 tiles)
    val gridRows = 12
    val gridCols = 8
    val totalTiles = gridRows * gridCols
    val touchedTiles = remember { mutableStateListOf<Boolean>().apply { repeat(totalTiles) { add(false) } } }
    val touchedCount = touchedTiles.count { it }
    val coveragePct = (touchedCount * 100) / totalTiles

    val pointerColors = remember {
        listOf(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF5252), // Red
            Color(0xFF00E676), // Green
            Color(0xFFFFD600), // Yellow
            Color(0xFF7C4DFF), // Purple
            Color(0xFFFF6D00), // Orange
            Color(0xFFE040FB), // Magenta
            Color(0xFF00B0FF), // Light Blue
            Color(0xFF76FF03), // Lime
            Color(0xFFFF4081)  // Pink
        )
    }

    Scaffold(
        topBar = {
            TestTopBar(
                title = "Test Multitouche & Tactile",
                status = status,
                onBack = onBack
            )
        },
        bottomBar = {
            TestVerdictBar(
                status = status,
                onPass = {
                    viewModel.setTestResult(DiagnosticTest.MULTITOUCH, TestStatus.PASSED)
                    onBack()
                },
                onFail = {
                    viewModel.setTestResult(DiagnosticTest.MULTITOUCH, TestStatus.FAILED)
                    onBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Multi-doigts ($maxPointersEver max)") },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) },
                    modifier = Modifier.testTag("tab_multitouch_points")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Grille tactile ($coveragePct%)") },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = null) },
                    modifier = Modifier.testTag("tab_touch_grid")
                )
            }

            if (selectedTab == 0) {
                // Multi-finger tracking tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Doigts simultanés : ${activePointers.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (activePointers.isNotEmpty()) DiagCyan else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Record détecté : $maxPointersEver doigt(s)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                activePointers.clear()
                                maxPointersEver = 0
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_multitouch_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Effacer", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi-touch interactive canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .pointerInteropFilter { motionEvent ->
                                when (motionEvent.actionMasked) {
                                    MotionEvent.ACTION_DOWN,
                                    MotionEvent.ACTION_POINTER_DOWN,
                                    MotionEvent.ACTION_MOVE,
                                    MotionEvent.ACTION_POINTER_UP,
                                    MotionEvent.ACTION_UP -> {
                                        activePointers.clear()
                                        val pointerCount = motionEvent.pointerCount
                                        if (pointerCount > maxPointersEver) {
                                            maxPointersEver = pointerCount
                                            viewModel.updateMultiTouchPointerCount(pointerCount)
                                            if (pointerCount >= 2) {
                                                viewModel.setTestResult(DiagnosticTest.MULTITOUCH, TestStatus.PASSED)
                                            }
                                        }

                                        for (i in 0 until pointerCount) {
                                            val pid = motionEvent.getPointerId(i)
                                            val px = motionEvent.getX(i)
                                            val py = motionEvent.getY(i)
                                            val pressure = motionEvent.getPressure(i)
                                            activePointers.add(TouchPointer(pid, px, py, pressure))
                                        }
                                    }
                                    MotionEvent.ACTION_CANCEL -> {
                                        activePointers.clear()
                                    }
                                }
                                true
                            }
                            .testTag("multitouch_canvas_box")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw grid lines for visual reference
                            val step = 60.dp.toPx()
                            var x = step
                            while (x < size.width) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1f
                                )
                                x += step
                            }
                            var y = step
                            while (y < size.height) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                                y += step
                            }

                            // Draw each active touch
                            activePointers.forEachIndexed { index, pointer ->
                                val color = pointerColors[pointer.id % pointerColors.size]
                                val baseRadius = 45.dp.toPx()

                                // Outer target ring
                                drawCircle(
                                    color = color.copy(alpha = 0.25f),
                                    radius = baseRadius + 20.dp.toPx(),
                                    center = Offset(pointer.x, pointer.y)
                                )
                                drawCircle(
                                    color = color,
                                    radius = baseRadius,
                                    center = Offset(pointer.x, pointer.y),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                // Crosshair
                                drawLine(
                                    color = color.copy(alpha = 0.7f),
                                    start = Offset(pointer.x - baseRadius - 10, pointer.y),
                                    end = Offset(pointer.x + baseRadius + 10, pointer.y),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = color.copy(alpha = 0.7f),
                                    start = Offset(pointer.x, pointer.y - baseRadius - 10),
                                    end = Offset(pointer.x, pointer.y + baseRadius + 10),
                                    strokeWidth = 2f
                                )
                                // Inner dot
                                drawCircle(
                                    color = color,
                                    radius = 10.dp.toPx(),
                                    center = Offset(pointer.x, pointer.y)
                                )
                            }
                        }

                        if (activePointers.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = DiagCyan.copy(alpha = 0.6f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Posez 2 à 10 doigts simultanément",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "L'écran suit la position et la pression de chaque point de contact.",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            // Coordinate badge
                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    activePointers.take(4).forEach { pt ->
                                        Text(
                                            text = "Point #${pt.id + 1} : (${pt.x.toInt()}px, ${pt.y.toInt()}px)",
                                            color = pointerColors[pt.id % pointerColors.size],
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (activePointers.size > 4) {
                                        Text(
                                            text = "+ ${activePointers.size - 4} autres points",
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Touch Grid Dead-Zone test tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Couverture de la dalle : $coveragePct%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (coveragePct > 80) DiagGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$touchedCount / $totalTiles cases testées",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                for (i in 0 until totalTiles) {
                                    touchedTiles[i] = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_grid_btn")
                        ) {
                            Text("Réinitialiser", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var gridPixelSize by remember { mutableStateOf(IntSize.Zero) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .onSizeChanged { gridPixelSize = it }
                            .pointerInteropFilter { motionEvent ->
                                if (gridPixelSize.width > 0 && gridPixelSize.height > 0) {
                                    val cellW = gridPixelSize.width.toFloat() / gridCols
                                    val cellH = gridPixelSize.height.toFloat() / gridRows

                                    for (i in 0 until motionEvent.pointerCount) {
                                        val px = motionEvent.getX(i)
                                        val py = motionEvent.getY(i)

                                        val col = (px / cellW).toInt().coerceIn(0, gridCols - 1)
                                        val row = (py / cellH).toInt().coerceIn(0, gridRows - 1)
                                        val index = row * gridCols + col
                                        if (index in 0 until totalTiles && !touchedTiles[index]) {
                                            touchedTiles[index] = true
                                        }
                                    }

                                    if (touchedTiles.count { it } >= (totalTiles * 0.90)) {
                                        viewModel.setTestResult(DiagnosticTest.MULTITOUCH, TestStatus.PASSED)
                                    }
                                }
                                true
                            }
                            .testTag("touch_deadzone_grid")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val tileWidth = size.width / gridCols
                            val tileHeight = size.height / gridRows

                            for (row in 0 until gridRows) {
                                for (col in 0 until gridCols) {
                                    val idx = row * gridCols + col
                                    val isTouched = touchedTiles[idx]
                                    val topLeft = Offset(col * tileWidth, row * tileHeight)
                                    val tileSize = Size(tileWidth, tileHeight)

                                    drawRect(
                                        color = if (isTouched) DiagGreen.copy(alpha = 0.85f) else Color(0xFF1E293B),
                                        topLeft = topLeft,
                                        size = tileSize
                                    )
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        topLeft = topLeft,
                                        size = tileSize,
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Glissez votre doigt partout pour vérifier les bordures et l'absence de zone morte tactile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

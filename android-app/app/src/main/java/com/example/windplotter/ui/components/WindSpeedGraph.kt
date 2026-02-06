package com.example.windplotter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.windplotter.data.Sample

@Composable
fun WindSpeedGraph(
    samples: List<Sample>,
    lineColor: Color = Color(0xFF2196F3),
    areaColor: Color = Color(0x332196F3),
    modifier: Modifier = Modifier
) {
    val maxSpeed = samples.maxOfOrNull { it.windSpeed }?.coerceAtLeast(15f) ?: 15f 
    // Increased default max to 15f to give some headroom above the 10m/s red line

    Canvas(modifier = modifier.padding(start = 24.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)) {
        val width = size.width
        val height = size.height
        
        val count = samples.size
        
        // Helper to convert speed to Y coordinate
        fun getY(speed: Float): Float {
            val ratio = (speed / maxSpeed).coerceIn(0f, 1f)
            return height - (ratio * height)
        }

        // --- 1. Draw Threshold Lines ---
        
        // 5m/s Warning Line (Yellow)
        val y5 = getY(5f)
        drawLine(
            color = Color.Yellow,
            start = Offset(0f, y5),
            end = Offset(width, y5),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Butt,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // 10m/s Danger Line (Red)
        val y10 = getY(10f)
        drawLine(
            color = Color.Red,
            start = Offset(0f, y10),
            end = Offset(width, y10),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Butt,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // --- 2. Draw Y-Axis Labels (Text) ---
        drawIntoCanvas { canvas ->
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24f // 10sp approx
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            
            // Draw 0, 5, 10, 15 (Max)
            val steps = listOf(0, 5, 10, 15, maxSpeed.toInt())
            steps.distinct().forEach { v ->
                if (v <= maxSpeed) {
                    val y = getY(v.toFloat())
                    // Draw text slightly offset to left
                    canvas.nativeCanvas.drawText("${v}m", -10f, y + 10f, textPaint)
                }
            }
        }

        if (count < 2) return@Canvas

        // X scale
        val stepX = width / (count - 1).coerceAtLeast(1)

        val path = Path()
        
        samples.forEachIndexed { index, sample ->
            val x = index * stepX
            val y = getY(sample.windSpeed)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw Fill Area
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            color = areaColor
        )
    }
}

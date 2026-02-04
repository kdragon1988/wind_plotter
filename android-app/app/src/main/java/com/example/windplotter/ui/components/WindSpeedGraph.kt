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
import androidx.compose.ui.unit.dp
import com.example.windplotter.data.Sample

@Composable
fun WindSpeedGraph(
    samples: List<Sample>,
    lineColor: Color = Color(0xFF2196F3),
    areaColor: Color = Color(0x332196F3),
    modifier: Modifier = Modifier
) {
    val maxSpeed = samples.maxOfOrNull { it.windSpeed }?.coerceAtLeast(10f) ?: 10f

    Canvas(modifier = modifier.padding(8.dp)) {
        val width = size.width
        val height = size.height
        
        val count = samples.size
        if (count < 2) return@Canvas

        // X scale: spreading samples across width
        val stepX = width / (count - 1).coerceAtLeast(1)

        val path = Path()
        
        samples.forEachIndexed { index, sample ->
            val x = index * stepX
            // Y scale: Invert Y (0 at bottom)
            val ratio = (sample.windSpeed / maxSpeed).coerceIn(0f, 1f)
            val y = height - (ratio * height)
            
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

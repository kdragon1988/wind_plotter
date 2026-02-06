package com.example.windplotter.ui.screens

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.windplotter.DJIConnectionManager
import com.example.windplotter.data.Sample
import com.example.windplotter.ui.components.WindSpeedGraph
import com.example.windplotter.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val colorBg = Color(0xFF04090D)
private val colorPanel = Color(0xCC0D1720)
private val colorPanel2 = Color(0xCC101E2A)
private val colorBorder = Color(0x8836D9F8)
private val colorAccent = Color(0xFF33E6FF)
private val colorText = Color(0xFFE9F7FF)
private val colorMuted = Color(0xFF8FA7B8)
private val colorGood = Color(0xFF57E9B2)
private val colorWarn = Color(0xFFFFB347)
private val colorDanger = Color(0xFFFF6079)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onMissionStopped: () -> Unit,
    onOpenReport: () -> Unit
) {
    val currentMission by viewModel.currentMission.collectAsState()
    val latestSample by viewModel.latestSample.collectAsState()
    val windSpeed by viewModel.currentWindSpeed.collectAsState()
    val windDirection by viewModel.currentWindDirection.collectAsState()
    val altitude by viewModel.currentAltitude.collectAsState()
    val missionSamples by viewModel.missionSamples.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val activeSession by viewModel.activeSessionIndex.collectAsState()

    val sdkRegistered by DJIConnectionManager.isRegistered.collectAsState()
    val productConnected by DJIConnectionManager.isProductConnected.collectAsState()
    val batteryPercent by DJIConnectionManager.batteryPercent.collectAsState()
    val cameraName by DJIConnectionManager.boundCamera.collectAsState()

    val windRisk = when {
        windSpeed >= 10f -> "DANGER"
        windSpeed >= 5f -> "CAUTION"
        else -> "NORMAL"
    }
    val windRiskColor = when {
        windSpeed >= 10f -> colorDanger
        windSpeed >= 5f -> colorWarn
        else -> colorGood
    }
    val logRows = remember(missionSamples) { missionSamples.takeLast(140).asReversed() }
    val latestLogId = logRows.firstOrNull()?.id ?: -1L
    val logListState = rememberLazyListState()
    var lastManualScrollAt by remember { mutableLongStateOf(0L) }
    var autoFollowLog by remember { mutableStateOf(true) }

    LaunchedEffect(logListState) {
        snapshotFlow { logListState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) {
                autoFollowLog = false
                lastManualScrollAt = System.currentTimeMillis()
            } else {
                val awayFromLatest = logListState.firstVisibleItemIndex > 0 || logListState.firstVisibleItemScrollOffset > 0
                if (awayFromLatest) {
                    lastManualScrollAt = System.currentTimeMillis()
                } else {
                    autoFollowLog = true
                }
            }
        }
    }

    LaunchedEffect(lastManualScrollAt) {
        if (lastManualScrollAt == 0L) return@LaunchedEffect
        delay(3000)
        val awayFromLatest = logListState.firstVisibleItemIndex > 0 || logListState.firstVisibleItemScrollOffset > 0
        if (!logListState.isScrollInProgress && awayFromLatest) {
            autoFollowLog = true
        }
    }

    LaunchedEffect(autoFollowLog, latestLogId) {
        if (autoFollowLog && logRows.isNotEmpty()) {
            logListState.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050B10), colorBg, Color(0xFF060C12))
                )
            )
            .padding(10.dp)
    ) {
        // top strip: one-line mission + one-line status
        Row(modifier = Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactTopPanel(modifier = Modifier.weight(0.92f).fillMaxHeight()) {
                val missionName = currentMission?.name ?: "No Active Mission"
                Text(
                    text = "MIS",
                    color = colorAccent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = missionName,
                    modifier = Modifier.weight(1f),
                    color = colorText,
                    fontWeight = FontWeight.Bold,
                    fontSize = compactMissionSize(missionName),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            CompactTopPanel(modifier = Modifier.weight(1.98f).fillMaxHeight()) {
                Text(
                    text = "STATUS",
                    color = colorAccent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusTag(
                        modifier = Modifier.weight(1f),
                        label = "LNK",
                        value = if (productConnected) "ON" else "OFF",
                        valueColor = if (productConnected) colorGood else colorDanger
                    )
                    StatusTag(
                        modifier = Modifier.weight(1.2f),
                        label = "SDK",
                        value = if (sdkRegistered) "READY" else "WAIT",
                        valueColor = if (sdkRegistered) colorGood else colorWarn
                    )
                    StatusTag(
                        modifier = Modifier.weight(0.95f),
                        label = "BAT",
                        value = batteryPercent?.let { "$it%" } ?: "--",
                        valueColor = colorText,
                        valueFontSize = 9.sp
                    )
                    StatusTag(
                        modifier = Modifier.weight(1.15f),
                        label = "CAM",
                        value = abbreviateCameraName(cameraName?.name),
                        valueColor = colorText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // middle strip: video | position+log | control buttons
        Row(modifier = Modifier.fillMaxWidth().weight(1.14f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassPanel(
                modifier = Modifier.weight(1.62f).fillMaxHeight(),
                title = "LIVE CAMERA FEED",
                bodyPadding = 0.dp
            ) {
                LiveVideoSurface(modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1.3f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    title = "FLIGHT INFO",
                    bodyPadding = 8.dp,
                    titleBottomSpacing = 4.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        FlightInfoLine("LAT", formatCoordinate(latestSample?.latitude))
                        FlightInfoLine("LON", formatCoordinate(latestSample?.longitude))
                        FlightInfoLine("ALT", String.format(Locale.US, "%.1f m", altitude))
                        FlightInfoLine("WIND", String.format(Locale.US, "%.1f m/s", windSpeed))
                        FlightInfoLine("DIR", directionToJapanese(windDirection))
                        FlightInfoLine("RISK", windRisk, windRiskColor)
                        FlightInfoLine("SESSION", activeSession?.let { "S$it" } ?: "--", colorAccent)
                        FlightInfoLine("MEASURE", if (isMeasuring) "RUNNING" else "STANDBY", if (isMeasuring) colorGood else colorMuted)
                    }
                }
            }

            Column(modifier = Modifier.width(224.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard(
                    modifier = Modifier.fillMaxWidth().weight(0.24f),
                    title = "HOME",
                    subtitle = "Return to mission list",
                    accent = Color(0xFFEF4B5B),
                    onClick = {
                        viewModel.stopMeasurement()
                        viewModel.stopMission()
                        onMissionStopped()
                    }
                )

                ActionCard(
                    modifier = Modifier.fillMaxWidth().weight(0.24f),
                    title = "REPORT",
                    subtitle = "Open measurement report",
                    accent = Color(0xFFFC8D31),
                    onClick = onOpenReport
                )

                ActionCard(
                    modifier = Modifier.fillMaxWidth().weight(0.52f),
                    title = if (isMeasuring) "STOP" else "MEASURE",
                    subtitle = if (isMeasuring) "Stop current measurement" else "Start measurement now",
                    accent = if (isMeasuring) colorWarn else Color(0xFF34E6C7),
                    onClick = {
                        if (isMeasuring) viewModel.stopMeasurement() else viewModel.startMeasurement()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // bottom strip: log | graph
        Row(modifier = Modifier.fillMaxWidth().weight(0.92f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassPanel(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "WIND LOG"
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    HeaderCell("TIME", Modifier.weight(1f))
                    HeaderCell("SPD", Modifier.weight(0.8f))
                    HeaderCell("DIR", Modifier.weight(0.7f))
                    HeaderCell("ALT", Modifier.weight(0.8f))
                    HeaderCell("S", Modifier.weight(0.5f))
                }
                Divider(color = colorBorder)
                LazyColumn(modifier = Modifier.fillMaxSize(), state = logListState) {
                    items(items = logRows, key = { it.id }) { s: Sample ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp)) {
                            Cell(formatTime(s.timestamp), Modifier.weight(1f))
                            Cell(String.format(Locale.US, "%.1f", s.windSpeed), Modifier.weight(0.8f))
                            Cell(directionToJapanese(s.windDirection), Modifier.weight(0.7f))
                            Cell(String.format(Locale.US, "%.1f", s.altitude), Modifier.weight(0.8f))
                            Cell("S${s.sessionIndex}", Modifier.weight(0.5f), colorAccent)
                        }
                        Divider(color = Color(0x2236D9F8))
                    }
                }
            }

            GlassPanel(
                modifier = Modifier.weight(1.7f).fillMaxHeight(),
                title = "WIND TREND"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000), RoundedCornerShape(8.dp))
                ) {
                    if (missionSamples.isEmpty()) {
                        Text("No wind data", color = colorMuted, modifier = Modifier.align(Alignment.Center))
                    } else {
                        WindSpeedGraph(samples = missionSamples, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTopPanel(
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorPanel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, colorBorder, RoundedCornerShape(12.dp))
                .background(colorPanel2.copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier,
    title: String,
    bodyPadding: Dp = 10.dp,
    titleBottomSpacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorPanel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, colorBorder, RoundedCornerShape(12.dp))
                .background(colorPanel2.copy(alpha = 0.35f))
                .padding(bodyPadding)
        ) {
            Text(
                text = title,
                color = colorAccent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(titleBottomSpacing))
            content()
        }
    }
}

@Composable
private fun StatusTag(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    labelFontSize: TextUnit = 7.sp,
    valueFontSize: TextUnit = 7.sp
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .background(Color(0x55000000), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x5536D9F8), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text("$label ", color = colorMuted, fontFamily = FontFamily.Monospace, fontSize = labelFontSize, maxLines = 1)
        Text(
            value,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = valueFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FlightInfoLine(
    label: String,
    value: String,
    valueColor: Color = colorText,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.width(58.dp),
            color = colorMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            maxLines = 1
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorPanel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, colorBorder, RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.28f),
                            Color(0x22000000)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    color = colorText,
                    fontWeight = FontWeight.Black,
                    fontSize = actionTitleSize(title),
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = colorText.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text,
        modifier = modifier,
        color = colorMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Cell(text: String, modifier: Modifier, color: Color = colorText) {
    Text(
        text,
        modifier = modifier,
        color = color,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun LiveVideoSurface(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            TextureView(context).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                        val surface = Surface(texture)
                        DJIConnectionManager.bindCameraStream(surface, width, height)
                        setTag(surface)
                    }

                    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
                        (getTag() as? Surface)?.let {
                            DJIConnectionManager.unbindCameraStream(it)
                            it.release()
                        }
                        val surface = Surface(texture)
                        DJIConnectionManager.bindCameraStream(surface, width, height)
                        setTag(surface)
                    }

                    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                        (getTag() as? Surface)?.let {
                            DJIConnectionManager.unbindCameraStream(it)
                            it.release()
                        }
                        setTag(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun MiniMap(samples: List<Sample>, modifier: Modifier = Modifier) {
    val valid = samples.filter { it.latitude != 0.0 && it.longitude != 0.0 }

    Box(
        modifier = modifier
            .background(Color(0xCC0A1723), RoundedCornerShape(8.dp))
            .border(1.dp, colorBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2f, h / 2f)
            val r = minOf(w, h) * 0.44f

            drawCircle(color = Color(0x2236D9F8), radius = r)
            drawCircle(color = Color(0x2236D9F8), radius = r * 0.66f)
            drawCircle(color = Color(0x2236D9F8), radius = r * 0.33f)
            drawLine(Color(0x2236D9F8), Offset(c.x, 0f), Offset(c.x, h), 1f)
            drawLine(Color(0x2236D9F8), Offset(0f, c.y), Offset(w, c.y), 1f)

            if (valid.size < 2) return@Canvas

            val lats = valid.map { it.latitude }
            val lons = valid.map { it.longitude }
            val minLat = lats.minOrNull() ?: return@Canvas
            val maxLat = lats.maxOrNull() ?: return@Canvas
            val minLon = lons.minOrNull() ?: return@Canvas
            val maxLon = lons.maxOrNull() ?: return@Canvas
            val latSpan = (maxLat - minLat).takeIf { it > 1e-9 } ?: 1e-9
            val lonSpan = (maxLon - minLon).takeIf { it > 1e-9 } ?: 1e-9

            fun toPoint(s: Sample): Offset {
                val px = ((s.longitude - minLon) / lonSpan).toFloat()
                val py = ((s.latitude - minLat) / latSpan).toFloat()
                val pad = 12f
                return Offset(
                    pad + px * (w - pad * 2),
                    h - (pad + py * (h - pad * 2))
                )
            }

            val path = Path()
            valid.forEachIndexed { i, s ->
                val p = toPoint(s)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }

            drawPath(path = path, color = colorAccent, style = Stroke(width = 2.6f, cap = StrokeCap.Round))
            drawCircle(color = colorDanger, radius = 5f, center = toPoint(valid.last()))
        }

        if (valid.isEmpty()) {
            Text("No GPS data", color = colorMuted, modifier = Modifier.align(Alignment.Center), fontSize = 12.sp)
        }
    }
}

private fun formatCoordinate(value: Double?): String {
    return if (value == null || value == 0.0) "--" else String.format(Locale.US, "%.6f", value)
}

private fun formatTime(ts: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
}

private fun compactMissionSize(text: String) = when {
    text.length <= 8 -> 16.sp
    text.length <= 14 -> 14.sp
    text.length <= 20 -> 12.sp
    else -> 11.sp
}

private fun actionTitleSize(text: String) = when {
    text.length <= 4 -> 22.sp
    text.length <= 7 -> 19.sp
    else -> 17.sp
}

private fun abbreviateCameraName(name: String?): String {
    val n = name ?: return "--"
    return when {
        n.contains("LEFT_OR_MAIN", ignoreCase = true) -> "MAIN"
        n.contains("MAIN", ignoreCase = true) -> "MAIN"
        n.contains("FPV", ignoreCase = true) -> "FPV"
        n.contains("ZOOM", ignoreCase = true) -> "ZOOM"
        n.length > 12 -> n.take(12)
        else -> n
    }
}

private fun directionToJapanese(directionDeg: Float): String {
    val normalized = ((directionDeg % 360f) + 360f) % 360f
    val index = (((normalized + 22.5f) % 360f) / 45f).toInt()
    return when (index) {
        0 -> "北"
        1 -> "北東"
        2 -> "東"
        3 -> "南東"
        4 -> "南"
        5 -> "南西"
        6 -> "西"
        else -> "北西"
    }
}

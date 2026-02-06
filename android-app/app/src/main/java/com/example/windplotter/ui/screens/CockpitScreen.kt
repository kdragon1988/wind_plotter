package com.example.windplotter.ui.screens

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.windplotter.DJIConnectionManager

@Composable
fun CockpitScreen(
    onClose: () -> Unit
) {
    val sdkRegistered = DJIConnectionManager.isRegistered.collectAsState()
    val productConnected = DJIConnectionManager.isProductConnected.collectAsState()
    val batteryPercent = DJIConnectionManager.batteryPercent.collectAsState()
    val boundCamera = DJIConnectionManager.boundCamera.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val surface = Surface(surfaceTexture)
                            DJIConnectionManager.bindCameraStream(surface, width, height)
                            setTag(surface)
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val existing = getTag() as? Surface
                            existing?.let { DJIConnectionManager.unbindCameraStream(it) }
                            val surface = Surface(surfaceTexture)
                            DJIConnectionManager.bindCameraStream(surface, width, height)
                            setTag(surface)
                        }

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            val existing = getTag() as? Surface
                            existing?.let {
                                DJIConnectionManager.unbindCameraStream(it)
                                it.release()
                            }
                            setTag(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        MaterialSurface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.55f),
            shape = CircleShape
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = if (sdkRegistered.value) "SDK: REGISTERED" else "SDK: NOT REGISTERED",
                    color = if (sdkRegistered.value) Color(0xFF7CFC00) else Color(0xFFFF6B6B)
                )
                Text(
                    text = if (productConnected.value) "AIRCRAFT: CONNECTED" else "AIRCRAFT: DISCONNECTED",
                    color = if (productConnected.value) Color(0xFF7CFC00) else Color(0xFFFF6B6B)
                )
                Text(
                    text = "BATTERY: ${batteryPercent.value?.let { "$it%" } ?: "--"}",
                    color = Color.White
                )
                Text(
                    text = "CAMERA: ${boundCamera.value?.name ?: "--"}",
                    color = Color.White
                )
            }
        }

        // Close Button (Top Right)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Red.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Cockpit",
                tint = Color.White
            )
        }
    }
}

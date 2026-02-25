package com.poolcoach.ui.camera

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CameraScreen(
    onSnapshotReady: (Bitmap) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.snapshot) {
        uiState.snapshot?.let { onSnapshotReady(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    viewModel.getCameraManager().bindCamera(lifecycleOwner, pv)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = when (uiState.status) {
                    CameraStatus.WAITING  -> "Waiting for break..."
                    CameraStatus.SETTLING -> "Balls settling..."
                    CameraStatus.READY    -> "Analyzing table..."
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) { Text("⚙", color = Color.White) }
        OutlinedButton(
            onClick = { },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        ) { Text("Capture Now") }
    }
}

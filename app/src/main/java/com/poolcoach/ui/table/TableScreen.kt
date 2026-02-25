package com.poolcoach.ui.table

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.poolcoach.domain.model.Shot

@Composable
fun TableScreen(
    onAnimateShot: (Shot) -> Unit,
    onBackToCamera: () -> Unit,
    viewModel: TableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        TableCanvas(
            renderState = TableRenderState(
                tableState = uiState.tableState,
                selectedBall = uiState.selectedBall,
                activeShot = uiState.activeShot
            ),
            onBallTapped = viewModel::onBallTapped,
            modifier = Modifier.fillMaxSize()
        )

        // Shot info overlay
        uiState.activeShot?.let { shot ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "${shot.hitFractionLabel} — ${String.format("%.0f°", shot.cutAngleDeg)} cut",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onAnimateShot(shot) }) { Text("▶ Animate") }
            }
        }

        // Back button
        IconButton(
            onClick = onBackToCamera,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Text("↩", color = Color.White)
        }
    }
}

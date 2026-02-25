package com.poolcoach.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poolcoach.domain.model.Shot
import com.poolcoach.domain.model.TableState
import com.poolcoach.ui.table.TableCanvas
import com.poolcoach.ui.table.TableRenderState

@Composable
fun ShotAnimationScreen(
    shot: Shot,
    tableState: TableState,
    onBack: () -> Unit
) {
    val animatable = remember { Animatable(0f) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
            )
            isPlaying = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TableCanvas(
            renderState = TableRenderState(
                tableState = tableState,
                selectedBall = shot.objectBall,
                activeShot = shot,
                animationProgress = animatable.value
            ),
            onBallTapped = {},
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { isPlaying = true }) {
                Text(if (isPlaying) "⏸ Playing..." else "▶ Play")
            }
            OutlinedButton(onClick = onBack) { Text("↩ Back") }
        }
    }
}

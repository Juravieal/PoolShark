package com.poolshark.ui.table

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.poolshark.domain.model.*
import kotlin.math.sqrt

private val BALL_COLORS = mapOf(
    1 to Color(0xFFFFD700), 2 to Color(0xFF0000CC), 3 to Color(0xFFCC0000),
    4 to Color(0xFF800080), 5 to Color(0xFFFF6600), 6 to Color(0xFF006600), 7 to Color(0xFF8B0000)
)
private val STRIPE_COLORS = mapOf(
    9 to Color(0xFFFFD700), 10 to Color(0xFF0000CC), 11 to Color(0xFFCC0000),
    12 to Color(0xFF800080), 13 to Color(0xFFFF6600), 14 to Color(0xFF006600), 15 to Color(0xFF8B0000)
)

@Composable
fun TableCanvas(
    renderState: TableRenderState,
    onBallTapped: (Ball) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val ts = renderState.tableState.tableSize

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(renderState) {
                detectTapGestures { offset ->
                    val r = ballRadiusPx(size.width.toFloat(), ts)
                    val tapped = renderState.tableState.objectBalls.firstOrNull { ball ->
                        val (bx, by) = tableToScreen(ball.tableX, ball.tableY, size.width.toFloat(), size.height.toFloat(), ts)
                        val dx = offset.x - bx; val dy = offset.y - by
                        sqrt(dx * dx + dy * dy) <= r
                    }
                    tapped?.let { onBallTapped(it) }
                }
            }
    ) {
        val w = size.width; val h = size.height
        val progress = renderState.animationProgress
        val shot = renderState.activeShot

        // Rail background + cloth
        drawRect(color = Color(0xFF5C3A1E), size = Size(w, h))
        val railW = w * 0.04f
        drawRect(color = Color(0xFF2D6A3F), topLeft = Offset(railW, railW), size = Size(w - railW * 2, h - railW * 2))

        // Pockets
        val pocketR = w * 0.025f
        ts.pockets.forEach { pocket ->
            val (px, py) = tableToScreen(pocket.tableX, pocket.tableY, w, h, ts)
            drawCircle(color = Color.Black, radius = pocketR, center = Offset(px, py))
        }

        // Head string
        val headY = tableToScreen(0f, ts.heightIn * 0.25f, w, h, ts).second
        drawLine(color = Color.White.copy(alpha = 0.2f), start = Offset(railW, headY), end = Offset(w - railW, headY), strokeWidth = 1f)

        // Balls
        val ballR = ballRadiusPx(w, ts)
        renderState.tableState.balls.forEach { ball ->
            val rawPos = tableToScreen(ball.tableX, ball.tableY, w, h, ts)
            val (bx, by) = when {
                shot != null && progress > 0f && ball == shot.cueBall -> {
                    val p1 = (progress * 2f).coerceIn(0f, 1f)
                    val (gx, gy) = tableToScreen(shot.ghostBallX, shot.ghostBallY, w, h, ts)
                    Pair(lerp(rawPos.first, gx, p1), lerp(rawPos.second, gy, p1))
                }
                shot != null && progress > 0.5f && ball == shot.objectBall -> {
                    val p2 = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
                    val (px, py) = tableToScreen(shot.targetPocket.tableX, shot.targetPocket.tableY, w, h, ts)
                    Pair(lerp(rawPos.first, px, p2), lerp(rawPos.second, py, p2))
                }
                else -> rawPos
            }
            val center = Offset(bx, by)

            if (ball == renderState.selectedBall) {
                drawCircle(color = Color.Yellow, radius = ballR * 1.4f, center = center, style = Stroke(width = 3f))
            }

            when (ball.type) {
                BallType.CUE_BALL -> drawCircle(color = Color.White, radius = ballR, center = center)
                BallType.EIGHT_BALL -> {
                    drawCircle(color = Color.Black, radius = ballR, center = center)
                    drawCircle(color = Color.White, radius = ballR * 0.4f, center = center)
                    drawBallLabel("8", center, ballR, textMeasurer, Color.Black)
                }
                BallType.SOLID -> {
                    val color = BALL_COLORS[ball.id] ?: Color.Gray
                    drawCircle(color = color, radius = ballR, center = center)
                    drawBallLabel(ball.id.toString(), center, ballR, textMeasurer, Color.White)
                }
                BallType.STRIPE -> {
                    drawCircle(color = Color.White, radius = ballR, center = center)
                    val color = STRIPE_COLORS[ball.id] ?: Color.Gray
                    drawCircle(color = color, radius = ballR * 0.6f, center = center)
                    drawCircle(color = Color.White, radius = ballR * 0.35f, center = center)
                    drawBallLabel(ball.id.toString(), center, ballR, textMeasurer, Color.Black)
                }
            }
            drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = ballR, center = center, style = Stroke(width = 1f))
        }

        // Shot overlay
        if (shot != null && progress == 0f) {
            val cb = tableToScreen(shot.cueBall.tableX, shot.cueBall.tableY, w, h, ts)
            val ghost = tableToScreen(shot.ghostBallX, shot.ghostBallY, w, h, ts)
            val ob = tableToScreen(shot.objectBall.tableX, shot.objectBall.tableY, w, h, ts)
            val pocket = tableToScreen(shot.targetPocket.tableX, shot.targetPocket.tableY, w, h, ts)
            drawLine(color = Color.White.copy(alpha = 0.7f), start = Offset(cb.first, cb.second), end = Offset(ghost.first, ghost.second), strokeWidth = 2f)
            drawCircle(color = Color.White.copy(alpha = 0.4f), radius = ballR, center = Offset(ghost.first, ghost.second), style = Stroke(width = 2f))
            drawLine(color = Color(0xFFFFD700).copy(alpha = 0.8f), start = Offset(ob.first, ob.second), end = Offset(pocket.first, pocket.second), strokeWidth = 2f)
            if (shot.cbDeflectionPath.size >= 2) {
                val d0 = tableToScreen(shot.cbDeflectionPath[0].first, shot.cbDeflectionPath[0].second, w, h, ts)
                val d1 = tableToScreen(shot.cbDeflectionPath[1].first, shot.cbDeflectionPath[1].second, w, h, ts)
                drawLine(
                    color = Color.Cyan.copy(alpha = 0.7f),
                    start = Offset(d0.first, d0.second), end = Offset(d1.first, d1.second),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                )
            }
        }

        // Cue face inset
        if (shot != null) {
            val insetR = w * 0.06f
            val cx = w * 0.88f; val cy = h * 0.15f
            drawCircle(color = Color(0xFFEEEEEE), radius = insetR, center = Offset(cx, cy))
            drawCircle(color = Color.Black, radius = insetR, center = Offset(cx, cy), style = Stroke(width = 2f))
            drawLine(Color.LightGray, Offset(cx - insetR, cy), Offset(cx + insetR, cy), strokeWidth = 1f)
            drawLine(Color.LightGray, Offset(cx, cy - insetR), Offset(cx, cy + insetR), strokeWidth = 1f)
            drawCircle(color = Color.Red, radius = insetR * 0.15f, center = Offset(cx + shot.cueStrikeX * insetR * 0.8f, cy + shot.cueStrikeY * insetR * 0.8f))
        }
    }
}

private fun DrawScope.drawBallLabel(text: String, center: Offset, r: Float, measurer: TextMeasurer, color: Color) {
    val style = TextStyle(color = color, fontSize = (r * 0.9f).sp, fontWeight = FontWeight.Bold)
    val measured = measurer.measure(text, style)
    drawText(measured, topLeft = Offset(center.x - measured.size.width / 2f, center.y - measured.size.height / 2f))
}

private fun tableToScreen(tableX: Float, tableY: Float, cw: Float, ch: Float, ts: TableSize): Pair<Float, Float> {
    val rail = cw * 0.04f
    val pw = cw - rail * 2f; val ph = ch - rail * 2f
    return Pair(rail + (tableX / ts.widthIn) * pw, rail + (tableY / ts.heightIn) * ph)
}

private fun ballRadiusPx(cw: Float, ts: TableSize): Float {
    val rail = cw * 0.04f
    return (1.125f / ts.widthIn) * (cw - rail * 2f)
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

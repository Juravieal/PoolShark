package com.poolshark.domain.shot

import kotlin.math.*

object ShotGeometry {

    fun cutAngle(cbX: Float, cbY: Float, obX: Float, obY: Float, pocketX: Float, pocketY: Float): Float {
        val ax = obX - cbX
        val ay = obY - cbY
        val bx = pocketX - obX
        val by = pocketY - obY
        val dot = ax * bx + ay * by
        val magnitudeA = sqrt(ax * ax + ay * ay)
        val magnitudeB = sqrt(bx * bx + by * by)
        if (magnitudeA == 0f || magnitudeB == 0f) return 0f
        val cosAngle = (dot / (magnitudeA * magnitudeB)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosAngle).toDouble()).toFloat()
    }

    fun ghostBallPos(obX: Float, obY: Float, pocketX: Float, pocketY: Float, ballDiameter: Float): Pair<Float, Float> {
        val dx = pocketX - obX
        val dy = pocketY - obY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist == 0f) return Pair(obX, obY)
        val nx = dx / dist
        val ny = dy / dist
        return Pair(obX - nx * ballDiameter, obY - ny * ballDiameter)
    }

    fun contactPoint(obX: Float, obY: Float, ghostX: Float, ghostY: Float, ballRadius: Float): Pair<Float, Float> {
        val dx = obX - ghostX
        val dy = obY - ghostY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist == 0f) return Pair(obX, obY)
        val nx = dx / dist
        val ny = dy / dist
        return Pair(obX - nx * ballRadius, obY - ny * ballRadius)
    }

    fun hitFractionLabel(cutAngleDeg: Float): String {
        return when {
            cutAngleDeg < 5f  -> "Full ball"
            cutAngleDeg < 22f -> "3/4 ball"
            cutAngleDeg < 40f -> "1/2 ball"
            cutAngleDeg < 65f -> "1/4 ball"
            else              -> "Thin cut"
        }
    }
}

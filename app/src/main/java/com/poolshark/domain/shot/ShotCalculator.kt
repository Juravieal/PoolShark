package com.poolshark.domain.shot

import com.poolshark.domain.model.*
import kotlin.math.*

class ShotCalculator {

    companion object {
        const val BALL_DIAMETER = 2.25f
        const val BALL_RADIUS = 1.125f
        const val MAX_CUT_ANGLE = 80f
        const val CB_DEFLECTION_LENGTH = 12f
    }

    fun calculateShots(cueBall: Ball, objectBall: Ball, tableSize: TableSize): List<Shot> {
        return tableSize.pockets
            .map { pocket -> calculateShot(cueBall, objectBall, pocket) }
            .filter { it.cutAngleDeg <= MAX_CUT_ANGLE }
            .sortedBy { it.cutAngleDeg }
    }

    fun calculateShot(cueBall: Ball, objectBall: Ball, pocket: Pocket): Shot {
        val cutAngle = ShotGeometry.cutAngle(
            cueBall.tableX, cueBall.tableY,
            objectBall.tableX, objectBall.tableY,
            pocket.tableX, pocket.tableY
        )
        val (ghostX, ghostY) = ShotGeometry.ghostBallPos(
            objectBall.tableX, objectBall.tableY,
            pocket.tableX, pocket.tableY,
            BALL_DIAMETER
        )
        val (contactX, contactY) = ShotGeometry.contactPoint(
            objectBall.tableX, objectBall.tableY,
            ghostX, ghostY,
            BALL_RADIUS
        )
        val deflectionPath = cbDeflectionPath(
            cueBall.tableX, cueBall.tableY,
            objectBall.tableX, objectBall.tableY,
            pocket.tableX, pocket.tableY
        )
        return Shot(
            cueBall, objectBall, pocket, cutAngle,
            ghostX, ghostY, contactX, contactY,
            deflectionPath, ShotGeometry.hitFractionLabel(cutAngle),
            0f, 0f
        )
    }

    private fun cbDeflectionPath(
        cbX: Float, cbY: Float,
        obX: Float, obY: Float,
        pocketX: Float, pocketY: Float
    ): List<Pair<Float, Float>> {
        val dx = pocketX - obX; val dy = pocketY - obY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist == 0f) return emptyList()
        val nx = dx / dist; val ny = dy / dist

        val perp1 = Pair(-ny, nx)
        val perp2 = Pair(ny, -nx)

        val cbDirX = obX - cbX; val cbDirY = obY - cbY
        val cbDist = sqrt(cbDirX * cbDirX + cbDirY * cbDirY)
        if (cbDist == 0f) return emptyList()
        val cbNx = cbDirX / cbDist; val cbNy = cbDirY / cbDist

        val chosenPerp = if (perp1.first * cbNx + perp1.second * cbNy > 0f) perp1 else perp2

        val startX = obX - nx * BALL_DIAMETER; val startY = obY - ny * BALL_DIAMETER
        val endX = startX + chosenPerp.first * CB_DEFLECTION_LENGTH
        val endY = startY + chosenPerp.second * CB_DEFLECTION_LENGTH
        return listOf(Pair(startX, startY), Pair(endX, endY))
    }
}

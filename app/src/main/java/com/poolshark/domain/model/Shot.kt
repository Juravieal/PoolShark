package com.poolshark.domain.model

data class Shot(
    val cueBall: Ball,
    val objectBall: Ball,
    val targetPocket: Pocket,
    val cutAngleDeg: Float,
    val ghostBallX: Float,
    val ghostBallY: Float,
    val contactPointX: Float,
    val contactPointY: Float,
    val cbDeflectionPath: List<Pair<Float, Float>>,
    val hitFractionLabel: String,
    val cueStrikeX: Float,
    val cueStrikeY: Float
)

package com.poolcoach.domain.model

enum class BallType { CUE_BALL, SOLID, STRIPE, EIGHT_BALL }

data class Ball(val id: Int, val type: BallType, val tableX: Float, val tableY: Float, val confidence: Float) {
    val isCueBall: Boolean get() = type == BallType.CUE_BALL
    val isObjectBall: Boolean get() = !isCueBall
}

package com.poolshark.domain.model

enum class GameMode { EIGHT_BALL, NINE_BALL, TEN_BALL }

enum class TableSize(val widthIn: Float, val heightIn: Float) {
    SEVEN_FOOT(78f, 39f),
    EIGHT_FOOT(92f, 46f),
    NINE_FOOT(100f, 50f);

    val pockets: List<Pocket>
        get() = listOf(
            Pocket(0, 0f, 0f),
            Pocket(1, widthIn / 2, 0f),
            Pocket(2, widthIn, 0f),
            Pocket(3, 0f, heightIn),
            Pocket(4, widthIn / 2, heightIn),
            Pocket(5, widthIn, heightIn)
        )
}

data class TableState(
    val balls: List<Ball>,
    val gameMode: GameMode = GameMode.EIGHT_BALL,
    val tableSize: TableSize = TableSize.NINE_FOOT
) {
    val cueBall: Ball? get() = balls.firstOrNull { it.isCueBall }
    val objectBalls: List<Ball> get() = balls.filter { it.isObjectBall }
}

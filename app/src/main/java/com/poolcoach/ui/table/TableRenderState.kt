package com.poolcoach.ui.table

import com.poolcoach.domain.model.Ball
import com.poolcoach.domain.model.Shot
import com.poolcoach.domain.model.TableState

data class TableRenderState(
    val tableState: TableState,
    val selectedBall: Ball?,
    val activeShot: Shot?,
    val animationProgress: Float = 0f
)

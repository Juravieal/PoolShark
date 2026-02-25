package com.poolshark.ui.table

import com.poolshark.domain.model.Ball
import com.poolshark.domain.model.Shot
import com.poolshark.domain.model.TableState

data class TableRenderState(
    val tableState: TableState,
    val selectedBall: Ball?,
    val activeShot: Shot?,
    val animationProgress: Float = 0f
)

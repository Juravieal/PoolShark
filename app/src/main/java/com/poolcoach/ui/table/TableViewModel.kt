package com.poolcoach.ui.table

import androidx.lifecycle.ViewModel
import com.poolcoach.domain.model.*
import com.poolcoach.domain.shot.ShotCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class TableUiState(
    val tableState: TableState = TableState(emptyList()),
    val selectedBall: Ball? = null,
    val activeShot: Shot? = null,
    val pottableShots: List<Shot> = emptyList()
)

@HiltViewModel
class TableViewModel @Inject constructor(
    private val shotCalculator: ShotCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState

    fun loadTable(tableState: TableState) {
        _uiState.value = TableUiState(tableState = tableState)
    }

    fun onBallTapped(ball: Ball) {
        if (ball.isCueBall) return
        val cueBall = _uiState.value.tableState.cueBall ?: return
        val shots = shotCalculator.calculateShots(cueBall, ball, _uiState.value.tableState.tableSize)
        _uiState.value = _uiState.value.copy(
            selectedBall = ball,
            activeShot = shots.firstOrNull(),
            pottableShots = shots
        )
    }

    fun onPocketSelected(pocket: Pocket) {
        val selectedBall = _uiState.value.selectedBall ?: return
        val cueBall = _uiState.value.tableState.cueBall ?: return
        val shot = shotCalculator.calculateShot(cueBall, selectedBall, pocket)
        _uiState.value = _uiState.value.copy(activeShot = shot)
    }

    fun onDeselect() {
        _uiState.value = _uiState.value.copy(
            selectedBall = null,
            activeShot = null,
            pottableShots = emptyList()
        )
    }
}

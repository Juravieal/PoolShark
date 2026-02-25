package com.poolshark.ui.table

import com.google.common.truth.Truth.assertThat
import com.poolshark.domain.model.*
import com.poolshark.domain.shot.ShotCalculator
import org.junit.Test

class TableViewModelTest {

    private val calculator = ShotCalculator()

    @Test
    fun `selecting an object ball computes shots`() {
        val balls = listOf(
            Ball(0, BallType.CUE_BALL, 50f, 40f, 0.9f),
            Ball(1, BallType.SOLID, 50f, 25f, 0.9f)
        )
        val state = TableState(balls = balls)
        val vm = TableViewModel(calculator)
        vm.loadTable(state)
        vm.onBallTapped(balls[1])

        assertThat(vm.uiState.value.selectedBall).isEqualTo(balls[1])
        assertThat(vm.uiState.value.activeShot).isNotNull()
    }

    @Test
    fun `tapping empty space deselects ball`() {
        val balls = listOf(
            Ball(0, BallType.CUE_BALL, 50f, 40f, 0.9f),
            Ball(1, BallType.SOLID, 50f, 25f, 0.9f)
        )
        val vm = TableViewModel(calculator)
        vm.loadTable(TableState(balls = balls))
        vm.onBallTapped(balls[1])
        vm.onDeselect()

        assertThat(vm.uiState.value.selectedBall).isNull()
        assertThat(vm.uiState.value.activeShot).isNull()
    }

    @Test
    fun `tapping cue ball does not select it`() {
        val balls = listOf(Ball(0, BallType.CUE_BALL, 50f, 40f, 0.9f))
        val vm = TableViewModel(calculator)
        vm.loadTable(TableState(balls = balls))
        vm.onBallTapped(balls[0])

        assertThat(vm.uiState.value.selectedBall).isNull()
    }
}

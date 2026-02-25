package com.poolcoach.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.poolcoach.data.cv.BallDetector
import com.poolcoach.domain.calibration.CalibrationManager
import com.poolcoach.domain.model.GameMode
import com.poolcoach.domain.model.TableSize
import com.poolcoach.domain.model.TableState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class AppSettings(
    val tableSize: TableSize = TableSize.NINE_FOOT,
    val gameMode: GameMode = GameMode.EIGHT_BALL
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val ballDetector: BallDetector,
    private val calibrationManager: CalibrationManager
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    fun onTableSizeChanged(size: TableSize) {
        _settings.value = _settings.value.copy(tableSize = size)
    }

    fun onGameModeChanged(mode: GameMode) {
        _settings.value = _settings.value.copy(gameMode = mode)
    }

    fun processSnapshot(bitmap: Bitmap): TableState {
        val cfg = _settings.value
        val balls = ballDetector.detect(
            bitmap,
            calibrationManager.currentCalibration,
            cfg.tableSize.widthIn,
            cfg.tableSize.heightIn
        )
        return TableState(balls = balls, tableSize = cfg.tableSize, gameMode = cfg.gameMode)
    }
}

package com.poolcoach.ui.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poolcoach.data.preferences.CalibrationRepository
import com.poolcoach.domain.calibration.CalibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalibrationUiState(
    val step: CalibrationStep = CalibrationStep.CORNER_SETUP,
    val breaksCompleted: Int = 0
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationManager: CalibrationManager,
    private val repository: CalibrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState

    fun onCornersSet(corners: List<Pair<Float, Float>>) {
        calibrationManager.setCorners(corners)
        _uiState.value = _uiState.value.copy(step = CalibrationStep.BREAK_SESSION)
    }

    fun onBreakCaptured() {
        val newCount = _uiState.value.breaksCompleted + 1
        calibrationManager.recordBreakDetectionConfidences(
            mapOf("CUE_BALL" to 0.7f, "SOLID" to 0.65f, "STRIPE" to 0.65f, "EIGHT_BALL" to 0.75f)
        )
        if (newCount >= 10) {
            calibrationManager.finalizeCalibration()
            viewModelScope.launch {
                repository.save(calibrationManager.currentCalibration)
            }
            _uiState.value = _uiState.value.copy(step = CalibrationStep.DONE)
        } else {
            _uiState.value = _uiState.value.copy(breaksCompleted = newCount)
        }
    }
}

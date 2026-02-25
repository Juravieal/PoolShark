package com.poolcoach.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poolcoach.data.camera.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CameraStatus { WAITING, SETTLING, READY }

data class CameraUiState(
    val status: CameraStatus = CameraStatus.WAITING,
    val snapshot: Bitmap? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState

    init {
        viewModelScope.launch {
            cameraManager.snapshots.collect { bitmap ->
                _uiState.value = CameraUiState(status = CameraStatus.READY, snapshot = bitmap)
            }
        }
    }

    fun getCameraManager(): CameraManager = cameraManager
}

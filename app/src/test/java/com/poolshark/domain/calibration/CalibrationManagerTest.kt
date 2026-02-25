package com.poolshark.domain.calibration

import com.google.common.truth.Truth.assertThat
import com.poolshark.domain.model.CalibrationData
import org.junit.Test

class CalibrationManagerTest {

    @Test
    fun `calibration is not complete until 4 corners set`() {
        val manager = CalibrationManager()
        assertThat(manager.currentCalibration.isComplete).isFalse()
    }

    @Test
    fun `setting 4 corners enables homography computation`() {
        val manager = CalibrationManager()
        manager.setCorners(listOf(0f to 0f, 100f to 0f, 100f to 50f, 0f to 50f))
        assertThat(manager.currentCalibration.tableCorners).hasSize(4)
    }

    @Test
    fun `confidence thresholds update after calibration session`() {
        val manager = CalibrationManager()
        manager.recordBreakDetectionConfidences(mapOf("CUE_BALL" to 0.7f, "SOLID" to 0.65f))
        manager.recordBreakDetectionConfidences(mapOf("CUE_BALL" to 0.75f, "SOLID" to 0.6f))
        manager.finalizeCalibration()
        val thresholds = manager.currentCalibration.confidenceThresholds
        assertThat(thresholds["CUE_BALL"]).isGreaterThan(0f)
    }
}

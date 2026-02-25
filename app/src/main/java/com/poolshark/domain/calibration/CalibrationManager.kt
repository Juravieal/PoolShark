package com.poolshark.domain.calibration

import com.poolshark.domain.model.CalibrationData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationManager @Inject constructor() {

    private var _calibration = CalibrationData()
    val currentCalibration: CalibrationData get() = _calibration

    private val confidenceObservations = mutableMapOf<String, MutableList<Float>>()

    fun setCorners(corners: List<Pair<Float, Float>>) {
        require(corners.size == 4)
        _calibration = _calibration.copy(tableCorners = corners)
    }

    fun updateHomography(tableWidthIn: Float, tableHeightIn: Float) {
        if (_calibration.tableCorners.size < 4) return
        val mapper = HomographyMapper.fromCorners(
            _calibration.tableCorners, tableWidthIn, tableHeightIn
        )
        // Store the computed matrix by applying the mapper's internal H field.
        // HomographyMapper exposes it via the matrix property added in Task 5.
        _calibration = _calibration.copy(homographyMatrix = mapper.matrix)
    }

    /**
     * Record per-class confidence scores observed during a break detection.
     * Called once per calibration break (up to 10 times).
     */
    fun recordBreakDetectionConfidences(scores: Map<String, Float>) {
        scores.forEach { (key, value) ->
            confidenceObservations.getOrPut(key) { mutableListOf() }.add(value)
        }
    }

    /**
     * After all calibration breaks, set thresholds to 80% of mean observed confidence.
     */
    fun finalizeCalibration() {
        val thresholds = confidenceObservations.mapValues { (_, values) ->
            values.average().toFloat() * 0.80f
        }
        _calibration = _calibration.copy(confidenceThresholds = thresholds)
        confidenceObservations.clear()
    }

    fun load(data: CalibrationData) {
        _calibration = data
    }
}

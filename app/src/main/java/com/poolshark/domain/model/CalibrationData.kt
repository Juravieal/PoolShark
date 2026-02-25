package com.poolshark.domain.model

data class CalibrationData(
    val homographyMatrix: FloatArray = FloatArray(9),
    val confidenceThresholds: Map<String, Float> = mapOf(
        "CUE_BALL" to 0.5f,
        "SOLID" to 0.5f,
        "STRIPE" to 0.5f,
        "EIGHT_BALL" to 0.5f
    ),
    val tableCorners: List<Pair<Float, Float>> = emptyList()
) {
    val isComplete: Boolean get() = tableCorners.size == 4 && homographyMatrix.any { it != 0f }
}

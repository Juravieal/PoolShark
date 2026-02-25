package com.poolshark.data.camera

import kotlin.math.abs

data class MotionResult(val delta: Float, val isStable: Boolean)

class MotionDetector(
    private val threshold: Float = 0.03f,
    private val stableFrames: Int = 45
) {
    private var previousFrame: ByteArray? = null
    private var stableCount = 0

    fun processFrame(frame: ByteArray, width: Int, stride: Int = 4): MotionResult {
        if (previousFrame == null || previousFrame!!.size != frame.size) {
            previousFrame = frame.copyOf()
            stableCount = 0
            return MotionResult(1f, false)
        }
        val prev = previousFrame!!
        var diff = 0L; var count = 0; var i = 0
        while (i < frame.size) {
            diff += abs(frame[i].toInt() - prev[i].toInt())
            count++; i += stride
        }
        val delta = if (count == 0) 0f else diff.toFloat() / (count * 255f)
        stableCount = if (delta < threshold) stableCount + 1 else 0
        val isStable = stableCount >= stableFrames
        previousFrame = frame.copyOf()
        return MotionResult(delta, isStable)
    }

    fun reset() {
        previousFrame = null
        stableCount = 0
    }
}

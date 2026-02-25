package com.poolshark.data.cv

import com.google.common.truth.Truth.assertThat
import com.poolshark.domain.model.BallType
import org.junit.Test

class BallDetectorTest {

    @Test
    fun `detection result maps class index to BallType`() {
        assertThat(BallDetector.classIndexToBallType(0)).isEqualTo(BallType.CUE_BALL)
        assertThat(BallDetector.classIndexToBallType(1)).isEqualTo(BallType.SOLID)
        assertThat(BallDetector.classIndexToBallType(2)).isEqualTo(BallType.STRIPE)
        assertThat(BallDetector.classIndexToBallType(3)).isEqualTo(BallType.EIGHT_BALL)
    }

    @Test
    fun `NMS removes overlapping detections`() {
        val boxes = listOf(
            DetectionBox(0.5f, 0.5f, 0.1f, 0.1f, 0.9f, 0),
            DetectionBox(0.51f, 0.51f, 0.1f, 0.1f, 0.8f, 0),  // overlaps above
            DetectionBox(0.8f, 0.8f, 0.1f, 0.1f, 0.85f, 1)    // separate
        )
        val result = BallDetector.nonMaxSuppression(boxes, iouThreshold = 0.5f)
        assertThat(result).hasSize(2)  // overlapping pair collapsed to 1
        assertThat(result[0].confidence).isEqualTo(0.9f)
    }
}

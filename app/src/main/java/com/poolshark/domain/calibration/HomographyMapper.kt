package com.poolshark.domain.calibration

import kotlin.math.*

class HomographyMapper(private val h: FloatArray) {

    init { require(h.size == 9) { "Homography matrix must be 3x3 (9 elements)" } }

    val matrix: FloatArray get() = h.copyOf()

    fun toTableCoords(pixelX: Float, pixelY: Float): Pair<Float, Float> {
        val w = h[6] * pixelX + h[7] * pixelY + h[8]
        val x = (h[0] * pixelX + h[1] * pixelY + h[2]) / w
        val y = (h[3] * pixelX + h[4] * pixelY + h[5]) / w
        return Pair(x, y)
    }

    companion object {
        fun fromCorners(
            cameraCorners: List<Pair<Float, Float>>,
            tableWidthIn: Float,
            tableHeightIn: Float
        ): HomographyMapper {
            require(cameraCorners.size == 4)
            val dst = listOf(
                0f to 0f,
                tableWidthIn to 0f,
                tableWidthIn to tableHeightIn,
                0f to tableHeightIn
            )
            return HomographyMapper(computeDLT(cameraCorners, dst))
        }

        private fun computeDLT(src: List<Pair<Float, Float>>, dst: List<Pair<Float, Float>>): FloatArray {
            val A = Array(8) { DoubleArray(9) }
            for (i in 0..3) {
                val (sx, sy) = src[i]
                val (dx, dy) = dst[i]
                val r1 = i * 2; val r2 = r1 + 1
                A[r1][0] = -sx.toDouble(); A[r1][1] = -sy.toDouble(); A[r1][2] = -1.0
                A[r1][3] = 0.0;            A[r1][4] = 0.0;            A[r1][5] = 0.0
                A[r1][6] = dx * sx.toDouble(); A[r1][7] = dx * sy.toDouble(); A[r1][8] = dx.toDouble()
                A[r2][0] = 0.0;            A[r2][1] = 0.0;            A[r2][2] = 0.0
                A[r2][3] = -sx.toDouble(); A[r2][4] = -sy.toDouble(); A[r2][5] = -1.0
                A[r2][6] = dy * sx.toDouble(); A[r2][7] = dy * sy.toDouble(); A[r2][8] = dy.toDouble()
            }
            // Form A^T * A (9x9)
            val ATA = Array(9) { i -> DoubleArray(9) { j -> (0..7).sumOf { k -> A[k][i] * A[k][j] } } }
            // Find column with smallest norm as null-space approximation
            var minIdx = 0; var minNorm = Double.MAX_VALUE
            for (j in 0..8) {
                val norm = (0..8).sumOf { i -> ATA[i][j] * ATA[i][j] }
                if (norm < minNorm) { minNorm = norm; minIdx = j }
            }
            val hVec = DoubleArray(9) { ATA[it][minIdx] }
            val scale = if (hVec[8] != 0.0) hVec[8] else 1.0
            return FloatArray(9) { (hVec[it] / scale).toFloat() }
        }
    }
}

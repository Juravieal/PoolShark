package com.poolshark.data.cv

import android.content.Context
import android.graphics.Bitmap
import com.poolshark.domain.model.Ball
import com.poolshark.domain.model.BallType
import com.poolshark.domain.model.CalibrationData
import com.poolshark.domain.calibration.HomographyMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

data class DetectionBox(
    val cx: Float, val cy: Float,      // center, normalized 0..1
    val w: Float, val h: Float,        // width/height, normalized
    val confidence: Float,
    val classIndex: Int
)

@Singleton
class BallDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val interpreter: Interpreter by lazy { loadModel() }

    private fun loadModel(): Interpreter {
        val assetFd = context.assets.openFd("models/billiard_detector.tflite")
        val stream = FileInputStream(assetFd.fileDescriptor)
        val buffer = stream.channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset, assetFd.declaredLength
        )
        return Interpreter(buffer, Interpreter.Options().apply { numThreads = 4 })
    }

    /**
     * Run inference on a bitmap snapshot. Returns detected balls in table coordinates.
     */
    fun detect(
        bitmap: Bitmap,
        calibration: CalibrationData,
        tableWidthIn: Float,
        tableHeightIn: Float
    ): List<Ball> {
        val input = ImagePreprocessor.preprocess(bitmap)

        // YOLOv8 output shape: [1, 5+numClasses, numBoxes]
        val numBoxes = 8400
        val numClasses = 4
        val output = Array(1) { Array(5 + numClasses) { FloatArray(numBoxes) } }
        interpreter.run(input, output)

        val rawBoxes = parseOutput(output[0], numBoxes, numClasses, calibration.confidenceThresholds)
        val filtered = nonMaxSuppression(rawBoxes, iouThreshold = 0.45f)

        val mapper = if (calibration.isComplete) HomographyMapper(calibration.homographyMatrix) else null

        return filtered.mapIndexed { idx, box ->
            val (tableX, tableY) = if (mapper != null) {
                mapper.toTableCoords(box.cx * bitmap.width, box.cy * bitmap.height)
            } else {
                Pair(box.cx * tableWidthIn, box.cy * tableHeightIn)
            }
            Ball(
                id = idx,
                type = classIndexToBallType(box.classIndex),
                tableX = tableX, tableY = tableY,
                confidence = box.confidence
            )
        }
    }

    private fun parseOutput(
        output: Array<FloatArray>,
        numBoxes: Int,
        numClasses: Int,
        thresholds: Map<String, Float>
    ): List<DetectionBox> {
        val boxes = mutableListOf<DetectionBox>()
        for (i in 0 until numBoxes) {
            val cx = output[0][i]; val cy = output[1][i]
            val w = output[2][i]; val h = output[3][i]
            var bestClass = 0; var bestConf = 0f
            for (c in 0 until numClasses) {
                val conf = output[4 + c][i]
                if (conf > bestConf) { bestConf = conf; bestClass = c }
            }
            val typeKey = classIndexToBallType(bestClass).name
            val threshold = thresholds[typeKey] ?: 0.5f
            if (bestConf >= threshold) {
                boxes.add(DetectionBox(cx, cy, w, h, bestConf, bestClass))
            }
        }
        return boxes
    }

    companion object {

        fun classIndexToBallType(index: Int): BallType = when (index) {
            0 -> BallType.CUE_BALL
            1 -> BallType.SOLID
            2 -> BallType.STRIPE
            3 -> BallType.EIGHT_BALL
            else -> BallType.SOLID
        }

        fun nonMaxSuppression(boxes: List<DetectionBox>, iouThreshold: Float): List<DetectionBox> {
            val sorted = boxes.sortedByDescending { it.confidence }.toMutableList()
            val result = mutableListOf<DetectionBox>()
            while (sorted.isNotEmpty()) {
                val best = sorted.removeAt(0)
                result.add(best)
                sorted.removeAll { iou(it, best) > iouThreshold }
            }
            return result
        }

        private fun iou(a: DetectionBox, b: DetectionBox): Float {
            val ax1 = a.cx - a.w/2; val ay1 = a.cy - a.h/2
            val ax2 = a.cx + a.w/2; val ay2 = a.cy + a.h/2
            val bx1 = b.cx - b.w/2; val by1 = b.cy - b.h/2
            val bx2 = b.cx + b.w/2; val by2 = b.cy + b.h/2
            val ix1 = max(ax1, bx1); val iy1 = max(ay1, by1)
            val ix2 = min(ax2, bx2); val iy2 = min(ay2, by2)
            if (ix2 < ix1 || iy2 < iy1) return 0f
            val intersection = (ix2 - ix1) * (iy2 - iy1)
            val aArea = a.w * a.h; val bArea = b.w * b.h
            return intersection / (aArea + bArea - intersection)
        }
    }
}

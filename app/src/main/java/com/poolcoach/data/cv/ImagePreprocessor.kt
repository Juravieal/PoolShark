package com.poolcoach.data.cv

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {

    const val INPUT_SIZE = 640

    /**
     * Resize bitmap to INPUT_SIZE×INPUT_SIZE and convert to normalized float ByteBuffer.
     * Format: [1, INPUT_SIZE, INPUT_SIZE, 3] NHWC, float32, values 0..1.
     */
    fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val buffer = ByteBuffer.allocateDirect(4 * 1 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixel = resized.getPixel(x, y)
                buffer.putFloat(Color.red(pixel) / 255f)
                buffer.putFloat(Color.green(pixel) / 255f)
                buffer.putFloat(Color.blue(pixel) / 255f)
            }
        }
        buffer.rewind()
        return buffer
    }
}

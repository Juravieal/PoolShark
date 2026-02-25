package com.poolshark.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val motionDetector = MotionDetector()
    private val executor = Executors.newSingleThreadExecutor()
    private val _snapshots = MutableSharedFlow<Bitmap>(extraBufferCapacity = 1)
    val snapshots: SharedFlow<Bitmap> = _snapshots

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        ProcessCameraProvider.getInstance(context).addListener({
            val provider = it as ProcessCameraProvider
            provider.unbindAll()
            val preview = Preview.Builder().build().also { p ->
                p.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { a ->
                    a.setAnalyzer(executor) { imageProxy ->
                        val yBuf = imageProxy.planes[0].buffer
                        val yBytes = ByteArray(yBuf.remaining()).also { b -> yBuf.get(b) }
                        val result = motionDetector.processFrame(yBytes, imageProxy.width)
                        if (result.isStable) {
                            _snapshots.tryEmit(imageProxyToBitmap(imageProxy))
                            motionDetector.reset()
                        }
                        imageProxy.close()
                    }
                }
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val planes = imageProxy.planes
        val yBuf = planes[0].buffer; val uBuf = planes[1].buffer; val vBuf = planes[2].buffer
        val ySize = yBuf.remaining(); val uSize = uBuf.remaining(); val vSize = vBuf.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuf.get(nv21, 0, ySize)
        vBuf.get(nv21, ySize, vSize)
        uBuf.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }
}

package app.myzel394.alibi.effect

import android.content.Context
import androidx.camera.core.CameraEffect
import androidx.core.util.Consumer

class WatermarkEffect(
    targets: Int,
    private val surfaceProcessor: WatermarkSurfaceProcessor,
    errorListener: Consumer<Throwable>,
) : CameraEffect(
    targets,
    surfaceProcessor.glExecutor,
    surfaceProcessor,
    errorListener,
) {

    constructor(
        context: Context,
        targets: Int = VIDEO_CAPTURE,
        errorListener: Consumer<Throwable> = Consumer {},
    ) : this(targets, WatermarkSurfaceProcessor(context), errorListener)

    fun init() {
        surfaceProcessor.init()
    }

    fun release() {
        surfaceProcessor.release()
    }
}

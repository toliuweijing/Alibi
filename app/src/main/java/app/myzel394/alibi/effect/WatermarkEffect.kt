package app.myzel394.alibi.effect

import android.content.Context
import androidx.camera.core.CameraEffect
import androidx.core.util.Consumer
import java.util.concurrent.atomic.AtomicBoolean

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

    private var released = AtomicBoolean(false)

    constructor(
        context: Context,
        targets: Int = VIDEO_CAPTURE,
        errorListener: Consumer<Throwable> = Consumer {},
    ) : this(targets, WatermarkSurfaceProcessor(context), errorListener)

    fun release() {
        if (released.getAndSet(true)) {
            return
        }
        surfaceProcessor.release()
    }
}

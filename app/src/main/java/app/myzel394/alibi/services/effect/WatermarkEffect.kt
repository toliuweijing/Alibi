package app.myzel394.alibi.services.effect

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import androidx.core.util.Consumer
import app.myzel394.alibi.services.effect.opengl.OpenGlRenderer
import java.util.concurrent.Executor

class WatermarkEffect(
    targets: Int = PREVIEW or VIDEO_CAPTURE,
    private val surfaceProcessor: WatermarkSurfaceProcessor = WatermarkSurfaceProcessor(),
    errorListener: Consumer<Throwable> = Consumer {},
) : CameraEffect(
    targets,
    surfaceProcessor.glExecutor,
    surfaceProcessor,
    errorListener,
) {
    fun init() {
        surfaceProcessor.init()
    }

    fun release() {
        surfaceProcessor.release()
    }
}

private const val TAG = "WatermarkSurfaceProcessor"
class WatermarkSurfaceProcessor(
    private val openGlRenderer: OpenGlRenderer = OpenGlRenderer(),
) : SurfaceProcessor, SurfaceTexture.OnFrameAvailableListener {

    private val glThread: HandlerThread = HandlerThread("GlThread").apply {
        start()
    }
    private val glHandler: Handler = Handler(glThread.looper)
    val glExecutor: Executor = Executor { glHandler.post(it) }

    private val texMatrix = FloatArray(16)

    private var inputSurface: InputSurface? = null

    class InputSurface(
        val surfaceTexture: SurfaceTexture,
        val surface: Surface,
    )

    override fun onInputSurface(request: SurfaceRequest) {
        val surfaceTexture = SurfaceTexture(openGlRenderer.texId).apply {
            setOnFrameAvailableListener(this@WatermarkSurfaceProcessor, glHandler)
            setDefaultBufferSize(request.resolution.width, request.resolution.height)
        }
        val surface = Surface(surfaceTexture)
        inputSurface = InputSurface(surfaceTexture, surface)

        request.provideSurface(surface, glExecutor) {
            surfaceTexture.setOnFrameAvailableListener(null)
            surfaceTexture.release()
            surface.release()
        }
    }

    override fun onOutputSurface(surfaceOutput: SurfaceOutput) {
        val surface = surfaceOutput.getSurface(glExecutor) {
            openGlRenderer.unregister(surfaceOutput)
            surfaceOutput.close()
        }
        openGlRenderer.register(surfaceOutput, surface)
    }

    fun init() = glHandler.post {
        try {
            openGlRenderer.init()
        } catch (e: IllegalStateException) {
            openGlRenderer.release()
            throw e
        }
    }

    fun release() = glHandler.post {
        inputSurface?.apply {
            surfaceTexture.setOnFrameAvailableListener(null)
            surfaceTexture.release()
            surface.release()
        }
        inputSurface = null
        openGlRenderer.release()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(texMatrix)
        openGlRenderer.draw(surfaceTexture.timestamp, texMatrix)
    }
}
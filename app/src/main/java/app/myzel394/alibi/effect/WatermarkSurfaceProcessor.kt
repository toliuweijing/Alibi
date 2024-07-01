package app.myzel394.alibi.effect

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import app.myzel394.alibi.effect.opengl.OpenGlRenderer
import java.util.concurrent.Executor

class WatermarkSurfaceProcessor(
    private val openGlRenderer: OpenGlRenderer,
) : SurfaceProcessor, SurfaceTexture.OnFrameAvailableListener {

    constructor(context: Context) : this(OpenGlRenderer(context))

    private val glThread: HandlerThread = HandlerThread("GlThread").apply {
        start()
    }

    private val glHandler: Handler = Handler(glThread.looper)

    private val texMatrix = FloatArray(16)

    private var inputSurface: InputSurface? = null

    val glExecutor: Executor = Executor { glHandler.post(it) }

    private class InputSurface(
        val surfaceTexture: SurfaceTexture,
        val surface: Surface,
    )

    init {
        glHandler.post {
            openGlRenderer.init()
        }
    }

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

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(texMatrix)
        openGlRenderer.draw(surfaceTexture.timestamp, texMatrix)
    }

    fun release() = glHandler.post {
        releaseInternal()
    }

    private fun releaseInternal() {
        inputSurface?.apply {
            surfaceTexture.setOnFrameAvailableListener(null)
            surfaceTexture.release()
            surface.release()
        }
        inputSurface = null
        openGlRenderer.release()
        glThread.quitSafely()
    }
}

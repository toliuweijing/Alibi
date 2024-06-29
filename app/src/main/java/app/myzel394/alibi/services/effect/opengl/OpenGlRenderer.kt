package app.myzel394.alibi.services.effect.opengl

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.Matrix
import android.view.Surface
import androidx.camera.core.SurfaceOutput

class OpenGlRenderer(
    private val context: Context,
    private val eglCore: EglCore = EglCore(),
    private val cameraRenderPass: CameraRenderPass = CameraRenderPass(),
    private val watermarkRenderPass: WatermarkRenderPass = WatermarkRenderPass(context),
) {

    private var tempSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    // TODO: decouple SurfaceOutput from this class
    private val surfaceMap: HashMap<SurfaceOutput, EGLSurface> = HashMap()
    private val identityMatrix = FloatArray(16).also {
        Matrix.setIdentityM(it, 0)
    }
    private var isInit = false

    val texId: Int get() {
        return cameraRenderPass.texId.also {
            check(it != -1) {
                "texId is not initialized"
            }
        }
    }

    fun init() {
        if (isInit) {
            return
        }
        isInit = true

        eglCore.init()
        tempSurface = eglCore.createPbufferSurface(1, 1)
        eglCore.makeCurrent(tempSurface)

        cameraRenderPass.init()
        watermarkRenderPass.init()
    }

    fun register(key: SurfaceOutput, surface: Surface) {
        surfaceMap.getOrPut(key) {
            eglCore.createWindowSurface(surface)
        }
    }

    fun unregister(key: Any) {
        surfaceMap.remove(key)?.let {
            eglCore.destroySurface(it)
        }
    }

    fun draw(timestampNs: Long, texMatrix: FloatArray) {
        surfaceMap.forEach { (surfaceOutput, eglSurface) ->
            eglCore.makeCurrent(eglSurface)

            cameraRenderPass.draw(texMatrix, identityMatrix, surfaceOutput.size)
            watermarkRenderPass.draw(identityMatrix, identityMatrix, surfaceOutput.size)

            eglCore.setPresentationTime(eglSurface, timestampNs)
            eglCore.swapBuffer(eglSurface)
        }
    }

    fun release() {
        if (!isInit) {
            return
        }
        isInit = false

        cameraRenderPass.release()
        watermarkRenderPass.release()

        surfaceMap.forEach {
            it.key.close()
            eglCore.destroySurface(it.value)
        }
        surfaceMap.clear()
        eglCore.destroySurface(tempSurface)
        eglCore.release()
    }
}
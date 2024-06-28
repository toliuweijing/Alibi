package app.myzel394.alibi.services.effect.opengl

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.Matrix
import android.view.Surface
import androidx.camera.core.SurfaceOutput

class OpenGlRenderer(
    private val eglCore: EglCore = EglCore(),
    private val cameraRenderPass: CameraRenderPass = CameraRenderPass(),
) {

    private var tempSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    // TODO: decouple SurfaceOutput from this class
    private val surfaceMap: HashMap<SurfaceOutput, EGLSurface> = HashMap()
    private val mvpMatrix = FloatArray(16).also {
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

            cameraRenderPass.setSurfaceSize(surfaceOutput.size.width, surfaceOutput.size.height)
            cameraRenderPass.draw(texMatrix, mvpMatrix)

            eglCore.setPresentationTime(eglSurface, timestampNs)
            eglCore.swapBuffer(eglSurface)
        }
    }

    fun release() {
        if (!isInit) {
            return
        }
        isInit = false

        surfaceMap.forEach {
            it.key.close()
            eglCore.destroySurface(it.value)
        }
        surfaceMap.clear()
        eglCore.destroySurface(tempSurface)
        eglCore.release()
    }
}
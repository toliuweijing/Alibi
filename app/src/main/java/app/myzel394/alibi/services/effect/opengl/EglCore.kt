package app.myzel394.alibi.services.effect.opengl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface

private const val TAG = "EglCore"

class EglCore {

    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var config: EGLConfig? = null
    private var context: EGLContext = EGL14.EGL_NO_CONTEXT

    fun init(sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT) {
        try {
            // EGLDisplay
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY).also {
                check(it != EGL14.EGL_NO_DISPLAY) {
                    "glGetDisplay fails, ${EGL14.eglGetError()}"
                }
            }
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1).also {
                check(it) {
                    "eglInitialize fails, ${EGL14.eglGetError()}"
                }
            }

            // EGLConfig
            val attribToConfig = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT or EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE,
            )
            val configs: Array<EGLConfig?> = arrayOf(null)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(
                display,
                attribToConfig,
                0,
                configs,
                0,
                1,
                numConfigs,
                0,
            ).also {
                check(it) {
                    "eglChooseConfig fails, ${EGL14.eglGetError()}"
                }
            }
            this.config = configs[0]

            // EGLContext
            val attribToContext = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, // GLES 2.0
                EGL14.EGL_NONE
            )
            val context = EGL14.eglCreateContext(
                display,
                configs[0],
                sharedContext,
                attribToContext,
                0,
            )

            this.context = context
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Fails to init, $e")
            release()
        }
    }

    fun createPbufferSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttribute = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE,
        )
        return EGL14.eglCreatePbufferSurface(display, config!!, surfaceAttribute, 0).also {
            check(it != EGL14.EGL_NO_SURFACE) {
                "createPbufferSurface fails, ${EGL14.eglGetError()}"
            }
        }
    }

    fun createWindowSurface(surface: Surface): EGLSurface {
        val surfaceAttribute = intArrayOf(
            EGL14.EGL_NONE,
        )
        return EGL14.eglCreateWindowSurface(display, config!!, surface, surfaceAttribute, 0).also {
            check(it != EGL14.EGL_NO_SURFACE) {
                "createWindowSurface fails, ${EGL14.eglGetError()}"
            }
        }
    }

    fun destroySurface(eglSurface: EGLSurface) {
        EGL14.eglDestroySurface(display, eglSurface).also {
            check(it) {
                "destroySurface fails, ${EGL14.eglGetError()}"
            }
        }
    }

    fun makeCurrent(surface: EGLSurface) {
        EGL14.eglMakeCurrent(display, surface, surface, context).also {
            check(it) {
                "makeCurrent fails on surface $surface, ${EGL14.eglGetError()}"
            }
        }
    }

    fun swapBuffer(eglSurface: EGLSurface) {
        EGL14.eglSwapBuffers(display, eglSurface).also {
            check(it) {
                "eglSwapBuffers fails, ${EGL14.eglGetError()}"
            }
        }
    }

    fun setPresentationTime(eglSurface: EGLSurface, timestampNs: Long) {
        EGLExt.eglPresentationTimeANDROID(display, eglSurface, timestampNs).also {
            check(it) {
                "eglPresentationTime fails, ${EGL14.eglGetError()}"
            }
        }
    }

    fun release() {
        if (display == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "EglCore is not initialized or already released")
            return
        }

        if (context != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(display, context)
            context = EGL14.EGL_NO_CONTEXT
        }

        config = null

        EGL14.eglReleaseThread()
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
    }
}
package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20
import android.util.Log

private const val TAG = "GlProgram"
private const val INVALID = -1

/**
 * Represents an opengl program for drawing 2D graphics.
 */
class GlProgram(
    vertexShaderSrc: String,
    fragmentShaderSrc: String,
    private val gles20: Gles20Wrapper = Gles20Wrapper(),
) {
    private var program: Int = INVALID
    private var vertexShader: Int = INVALID
    private var fragmentShader: Int = INVALID

    init {
        try {
            program = gles20.glCreateProgram()
            vertexShader = gles20.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc, program)
            fragmentShader = gles20.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc, program)
            gles20.glLinkProgram(program)
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Fails to init program, $e")
            release()
        }
    }

    fun release() {
        if (program != INVALID) {
            gles20.glDeleteProgram(program)
            program = INVALID
        }
        if (vertexShader != INVALID) {
            gles20.glDeleteShader(vertexShader)
            vertexShader = INVALID
        }
        if (fragmentShader != INVALID) {
            gles20.glDeleteShader(fragmentShader)
            fragmentShader = INVALID
        }
    }
}
package app.myzel394.alibi.effect.opengl.core

import android.util.Log

private const val TAG = "GlProgram"
private const val INVALID = -1

/**
 * Represents an opengl program for drawing 2D graphics.
 */
class GlProgram(
    private val vertexShader: GlVertexShader,
    private val fragmentShader: GlFragmentShader,
    private val gles20: Gles20Wrapper = Gles20Wrapper(),
) {
    var programId: Int = INVALID
        private set

    fun init() {
        try {
            programId = gles20.glCreateProgram()
            gles20.glAttachShader(programId, vertexShader.shaderId)
            gles20.glAttachShader(programId, fragmentShader.shaderId)
            gles20.glLinkProgram(programId)
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Fails to init program, $e")
            release()
        }
    }

    fun use() {
        gles20.glUseProgram(programId)
    }

    fun release() {
        if (programId != INVALID) {
            gles20.glDeleteProgram(programId)
            programId = INVALID
        }
    }
}
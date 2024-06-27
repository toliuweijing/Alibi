package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20
import java.nio.Buffer

/**
 * Execute gl commands via GLES20 and check for errors before returning.
 */
class Gles20Wrapper {

    fun glCreateProgram(): Int {
        return GLES20.glCreateProgram().also {
            checkGlError("glCreateProgram", it)
        }
    }

    fun glCreateShader(type: Int): Int {
        return GLES20.glCreateShader(type).also {
            checkGlError("glCreateShader", it)
        }
    }

    fun glShaderSource(shader: Int, source: String) {
        GLES20.glShaderSource(shader, source)
        checkGlError("glShaderSource")
    }

    fun glCompileShader(shader: Int) {
        GLES20.glCompileShader(shader)
        checkGlError("glCompileShader")
        checkShaderCompileStatus(shader)
    }

    fun glAttachShader(program: Int, shader: Int) {
        GLES20.glAttachShader(program, shader)
        checkGlError("glAttachShader")
    }

    fun glDeleteShader(shader: Int) {
        GLES20.glDeleteShader(shader)
    }

    fun glLinkProgram(program: Int) {
        GLES20.glLinkProgram(program)
        checkLinkStatus(program)
    }

    fun glDeleteProgram(program: Int) {
        GLES20.glDeleteProgram(program)
    }

    fun glGetAttribLocation(program: Int, name: String): Int {
        return GLES20.glGetAttribLocation(program, name).also {
            checkGlError("glGetAttribLocation: $name")
        }
    }

    fun glGetUniformLocation(program: Int, name: String): Int {
        return GLES20.glGetUniformLocation(program, name).also {
            checkGlError("glGetUniformLocation: $name")
        }
    }

    fun glUseProgram(program: Int) {
        GLES20.glUseProgram(program)
        checkGlError("glUseProgram")
    }

    fun glEnableVertexAttribArray(location: Int) {
        GLES20.glEnableVertexAttribArray(location)
        checkGlError("glEnableVertexAttribArray")
    }

    fun glVertexAttribPointer(
        location: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        buffer: Buffer,
    ) {
        GLES20.glVertexAttribPointer(location, size, type, normalized, stride, buffer)
        checkGlError("glVertexAttribPointer")
    }

    private fun checkLinkStatus(program: Int) {
        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) {
            "Could not link program, ${GLES20.glGetProgramInfoLog(program)}"
        }
    }

    private fun checkShaderCompileStatus(shader: Int) {
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] != 0) {
            "Fails to compile shader $shader, info ${GLES20.glGetShaderInfoLog(shader)}"
        }
    }

    private fun checkGlError(description: String, result: Int? = null) {
        val error = GLES20.glGetError()
        result?.let {
            check(it > 0) {
                "Fails to execute $description with result $result, error $error"
            }
        } ?: run {
            check(error == GLES20.GL_NO_ERROR) {
                "Fails to execute $description, error $error"
            }
        }
    }

    companion object {
        val DEFAULT = Gles20Wrapper()
    }
}
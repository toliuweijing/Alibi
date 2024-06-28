package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun Gles20Wrapper.loadShader(shaderType: Int, shaderSource: String, program: Int? = null): Int {
    var shader = -1
    try {
        shader = glCreateShader(shaderType)
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        program?.run {
            glAttachShader(program, shader)
        }
        return shader
    } catch (e: IllegalStateException) {
        throw e
    } finally {
        if (shader != -1) {
            glDeleteShader(shader)
        }
    }
}

fun Gles20Wrapper.createTexture(type: Int): Int {
    val result = IntArray(1)
    glGenTextures(1, result, 0)
    glBindTexture(type, result[0])
    glTexParameter(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
    glTexParameter(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    glTexParameter(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    glTexParameter(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    return result[0]
}

val SIZEOF_FLOAT = 4

fun FloatArray.toFloatBuffer(): FloatBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(size * SIZEOF_FLOAT)
    byteBuffer.order(ByteOrder.nativeOrder())
    val floatBuffer = byteBuffer.asFloatBuffer()
    floatBuffer.put(this)
    floatBuffer.position(0)
    return floatBuffer
}

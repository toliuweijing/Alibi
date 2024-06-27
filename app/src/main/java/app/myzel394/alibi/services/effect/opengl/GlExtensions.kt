package app.myzel394.alibi.services.effect.opengl

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

val SIZEOF_FLOAT = 4

fun FloatArray.toFloatBuffer(): FloatBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(size * SIZEOF_FLOAT)
    byteBuffer.order(ByteOrder.nativeOrder())
    val floatBuffer = byteBuffer.asFloatBuffer()
    floatBuffer.put(this)
    floatBuffer.position(0)
    return floatBuffer
}

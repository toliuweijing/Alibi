package app.myzel394.alibi.services.effect.opengl

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

package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20

sealed class GlShader(
    private val shaderType: Int,
    private val shaderSource: String,
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) {
    val shader: Int = gles20.loadShader(shaderType, shaderSource)

    fun release() = gles20.glDeleteShader(shader)
}

class DefaultVertexShader(
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) : GlShader(
    GLES20.GL_VERTEX_SHADER,
    ShaderSource.VERTEX_DEFAULT,
) {
    var aPosition = -1
    var aTexCoords = -1
    var uMvpMatrix = -1
    var uTexMatrix = -1

    fun loadLocations(program: Int) {
        aPosition = gles20.glGetAttribLocation(program, "aPosition")
        aTexCoords = gles20.glGetAttribLocation(program, "aTexCoord")
        uTexMatrix = gles20.glGetUniformLocation(program, "uTexMatrix")
        uMvpMatrix = gles20.glGetUniformLocation(program, "uPosMatrix")
    }
}

class FragmentTextureExtShader(
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) : GlShader(
    GLES20.GL_FRAGMENT_SHADER,
    ShaderSource.FRAGMENT_TEXTURE_EXT
)

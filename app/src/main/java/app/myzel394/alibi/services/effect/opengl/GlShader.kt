package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES11Ext
import android.opengl.GLES20

sealed class GlShader(
    private val shaderType: Int,
    private val shaderSource: String,
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) {
    var shaderId: Int = GL_INVALID

    fun init() {
        shaderId = gles20.loadShader(shaderType, shaderSource)
    }

    fun release() {
        if (shaderId != GL_INVALID) {
            gles20.glDeleteShader(shaderId)
            shaderId = GL_INVALID
        }
    }
}

abstract class GlVertexShader(
    private val shaderType: Int,
    private val shaderSource: String,
) : GlShader(shaderType, shaderSource)

abstract class GlFragmentShader(
    private val shaderType: Int,
    private val shaderSource: String,
) : GlShader(shaderType, shaderSource)

class DefaultVertexShader(
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) : GlVertexShader(
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

class FragmentTextureShader(
    private val shaderSource: String,
) : GlFragmentShader(
    GLES20.GL_FRAGMENT_SHADER,
    shaderSource,
) {
    val textureType get() = if (shaderSource == ShaderSource.FRAGMENT_TEXTURE_EXT) {
        GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    } else {
        GLES20.GL_TEXTURE_2D
    }
}

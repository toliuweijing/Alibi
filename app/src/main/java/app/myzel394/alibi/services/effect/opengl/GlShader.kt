package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES11Ext
import android.opengl.GLES20

sealed class GlShader(
    private val shaderType: Int,
    private val shaderSource: String,
    private val gles20: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) {
    var shaderId: Int = GL_INVALID
        private set

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
) : GlShader(shaderType, shaderSource) {

    abstract fun loadLocations(program: Int)

    abstract fun configureAttributes()
}

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
        private set

    var aTexCoords = -1
        private set

    var uMvpMatrix = -1
        private set

    var uTexMatrix = -1
        private set

    override fun loadLocations(program: Int) {
        aPosition = gles20.glGetAttribLocation(program, "aPosition")
        aTexCoords = gles20.glGetAttribLocation(program, "aTexCoords")
        uTexMatrix = gles20.glGetUniformLocation(program, "uTexMatrix")
        uMvpMatrix = gles20.glGetUniformLocation(program, "uMvpMatrix")
    }

    override fun configureAttributes() {
        gles20.glEnableVertexAttribArray(aPosition)
        gles20.glVertexAttribPointer(
            aPosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            GlCoordinates.VERTEX_COORDS,
        )
        gles20.glEnableVertexAttribArray(aTexCoords)
        gles20.glVertexAttribPointer(
            aTexCoords,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            GlCoordinates.TEX_COORDS,
        )
    }
}

class FragmentTextureShader(
    private val shaderSource: String,
) : GlFragmentShader(
    GLES20.GL_FRAGMENT_SHADER,
    shaderSource,
) {
    val textureType
        get() = if (shaderSource == ShaderSource.FRAGMENT_TEXTURE_EXT) {
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        } else {
            GLES20.GL_TEXTURE_2D
        }
}

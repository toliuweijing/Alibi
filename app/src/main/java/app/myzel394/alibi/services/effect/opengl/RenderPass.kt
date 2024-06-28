package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20

interface RenderPass {

    fun init()

    fun setSurfaceSize(width: Int, height: Int)

    fun draw(
        texMatrix: FloatArray,
        mvpMatrix: FloatArray,
    )

    fun release()
}

class CameraRenderPass(
    private val gles20Wrapper: Gles20Wrapper = Gles20Wrapper.DEFAULT
) : RenderPass {

    private val vertexShader = DefaultVertexShader()
    private val fragmentShader = FragmentTextureShader(
        ShaderSource.FRAGMENT_TEXTURE_EXT
    )
    private val program: GlProgram = GlProgram(
        vertexShader,
        fragmentShader,
    )
    private var width: Int = -1
    private var height: Int = -1

    var texId: Int = -1
        private set

    override fun init() {
        vertexShader.init()
        fragmentShader.init()
        program.init()
        vertexShader.loadLocations(program.programId)
        configureProgram(program, vertexShader)
        texId = gles20Wrapper.createTexture(fragmentShader.textureType)
    }

    override fun setSurfaceSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun draw(texMatrix: FloatArray, mvpMatrix: FloatArray) {
        gles20Wrapper.glViewport(0, 0, width, height)
        gles20Wrapper.glUseProgram(program.programId)
        gles20Wrapper.glUniformMatrix4fv(
            vertexShader.uTexMatrix,
            1,
            false,
            texMatrix,
            0
        )
        gles20Wrapper.glUniformMatrix4fv(
            vertexShader.uMvpMatrix,
            1,
            false,
            mvpMatrix,
            0
        )
        gles20Wrapper.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable program
        gles20Wrapper.glUseProgram(0)
    }

    private fun configureProgram(program: GlProgram, vertexShader: DefaultVertexShader) {
        gles20Wrapper.glUseProgram(program.programId)
        gles20Wrapper.glEnableVertexAttribArray(vertexShader.aPosition)
        gles20Wrapper.glVertexAttribPointer(
            vertexShader.aPosition,
            4,
            GLES20.GL_FLOAT,
            false,
            0, // No interleaving
            GlCoordinates.VERTEX_COORDS,
        )
        gles20Wrapper.glEnableVertexAttribArray(vertexShader.aTexCoords)
        gles20Wrapper.glVertexAttribPointer(
            vertexShader.aTexCoords,
            4,
            GLES20.GL_FLOAT,
            false,
            0, // No interleaving
            GlCoordinates.TEX_COORDS,
        )
    }

    override fun release() {
        program.release()
        vertexShader.release()
        fragmentShader.release()
        if (texId != -1) {
            gles20Wrapper.glDeleteTextures(1, intArrayOf(texId), 0)
            texId = -1
        }
    }
}
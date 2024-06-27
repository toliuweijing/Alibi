package app.myzel394.alibi.services.effect.opengl

import android.opengl.GLES20

interface RenderPass {

    fun init()

    fun draw()

    fun release()
}

class CameraRenderPass(
    private val gles20Wrapper: Gles20Wrapper = Gles20Wrapper.DEFAULT
) : RenderPass {

    private var program: GlProgram? = null
    private var vertexShader: DefaultVertexShader? = null
    private var fragmentShader: FragmentTextureExtShader? = null

    override fun init() {
        val vertexShader = DefaultVertexShader().also {
            this.vertexShader = it
        }
        val fragmentShader = FragmentTextureExtShader().also {
            this.fragmentShader = it
        }
        val program = GlProgram(vertexShader, fragmentShader).also {
            this.program = it
        }
        vertexShader.loadLocations(program.programId)

        configureProgram(program, vertexShader)
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

    override fun draw() {
        TODO("Not yet implemented")
    }

    override fun release() {
        program?.release()
        program = null

        vertexShader?.release()
        vertexShader = null

        fragmentShader?.release()
        fragmentShader = null
    }


}
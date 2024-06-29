package app.myzel394.alibi.effect.opengl.renderpass

import android.opengl.GLES20
import android.util.Size
import app.myzel394.alibi.effect.opengl.core.DefaultVertexShader
import app.myzel394.alibi.effect.opengl.core.FragmentTextureShader
import app.myzel394.alibi.effect.opengl.core.GlProgram
import app.myzel394.alibi.effect.opengl.core.Gles20Wrapper
import app.myzel394.alibi.effect.opengl.ShaderSource
import app.myzel394.alibi.effect.opengl.core.createTexture

/**
 * Used to render a camera frame.
 */
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

    var texId: Int = -1
        private set

    override fun init() {
        vertexShader.init()
        fragmentShader.init()
        program.init()
        configureProgram(program, vertexShader)
        texId = gles20Wrapper.createTexture(fragmentShader.textureType)
    }

    override fun draw(texMatrix: FloatArray, mvpMatrix: FloatArray, surfaceSize: Size) {
        gles20Wrapper.glViewport(0, 0, surfaceSize.width, surfaceSize.height)
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
        vertexShader.loadLocations(program.programId)
        vertexShader.configureAttributes()
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

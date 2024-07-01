package app.myzel394.alibi.effect.opengl.renderpass

import android.util.Size

/**
 * Represents an opengl rendering abstraction.
 */
interface RenderPass {

    fun init()

    fun draw(
        texMatrix: FloatArray,
        mvpMatrix: FloatArray,
        surfaceSize: Size,
    )

    fun release()
}

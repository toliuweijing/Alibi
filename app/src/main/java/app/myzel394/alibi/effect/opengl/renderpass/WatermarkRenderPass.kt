package app.myzel394.alibi.effect.opengl.renderpass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Size
import app.myzel394.alibi.effect.opengl.ShaderSource
import app.myzel394.alibi.effect.opengl.core.DefaultVertexShader
import app.myzel394.alibi.effect.opengl.core.FragmentTextureShader
import app.myzel394.alibi.effect.opengl.core.GL_INVALID
import app.myzel394.alibi.effect.opengl.core.GlProgram
import app.myzel394.alibi.effect.opengl.core.Gles20Wrapper
import app.myzel394.alibi.effect.opengl.core.createTexture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TEXT_SIZE_MULTIPLY_VIDEO_SCREEN_RATIO = 30
private const val TAG = "WatermarkRenderPass"

/**
 * Used to render a watermark.
 */
class WatermarkRenderPass(
    private val context: Context,
    private val vertexShader: DefaultVertexShader,
    private val fragmentShader: FragmentTextureShader,
    private val program: GlProgram,
    private val gles20Wrapper: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) : RenderPass {

    constructor(
        context: Context,
        vertexShader: DefaultVertexShader =
            DefaultVertexShader(),
        fragmentShader: FragmentTextureShader =
            FragmentTextureShader(ShaderSource.FRAGMENT_TEXTURE_2D),
    ) : this(context, vertexShader, fragmentShader, GlProgram(vertexShader, fragmentShader))

    private var texId: Int = GL_INVALID

    private val textFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.US);

    private val paint = Paint().apply {
        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        isAntiAlias = true
        color = Color.WHITE
    }

    private var bitmap: Bitmap? = null

    private var canvas: Canvas? = null

    override fun init() {
        vertexShader.init()
        fragmentShader.init()
        program.init()

        program.use()
        vertexShader.loadLocations(program.programId)
        vertexShader.configureAttributes()
        texId = gles20Wrapper.createTexture(GLES20.GL_TEXTURE_2D)
    }

    override fun draw(texMatrix: FloatArray, mvpMatrix: FloatArray, surfaceSize: Size) {
        program.use()

        gles20Wrapper.glEnable(GLES20.GL_BLEND)
        gles20Wrapper.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        gles20Wrapper.glActiveTexture(GLES20.GL_TEXTURE0)
        gles20Wrapper.glBindTexture(fragmentShader.textureType, texId)

        val bitmap = drawTextToBitmap(surfaceSize)

        gles20Wrapper.glViewport(0, 0, bitmap.width, bitmap.height)
        GLUtils.texImage2D(fragmentShader.textureType, 0, bitmap, 0)

        vertexShader.configureMatrix(texMatrix, mvpMatrix)
        gles20Wrapper.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        gles20Wrapper.glDisable(GLES20.GL_BLEND)
    }

    private fun drawTextToBitmap(surfaceSize: Size): Bitmap {
        val text = textFormat.format(Date())

        val bitmap = this.bitmap ?: let {
            val videoToScreenRatio = surfaceSize.height * 1f / context.resources.displayMetrics.heightPixels
            paint.textSize = videoToScreenRatio * TEXT_SIZE_MULTIPLY_VIDEO_SCREEN_RATIO
            paint.setShadowLayer(videoToScreenRatio * 1f, 0f, 0f, Color.BLACK)

            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        }
        this.bitmap = bitmap

        val canvas = this.canvas ?: Canvas(bitmap)
        this.canvas = canvas

        bitmap.eraseColor(Color.TRANSPARENT)
        canvas.drawText(text, 0f, bitmap.height * 1f, paint)
        return bitmap
    }

    override fun release() {
        program.release()
        vertexShader.release()
        fragmentShader.release()
        bitmap = null
        canvas = null
    }
}

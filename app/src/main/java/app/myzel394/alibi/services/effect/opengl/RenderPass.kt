package app.myzel394.alibi.services.effect.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Size
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface RenderPass {

    fun init()

    fun draw(
        texMatrix: FloatArray,
        mvpMatrix: FloatArray,
        surfaceSize: Size,
    )

    fun release()
}

class WatermarkRenderPass(
    private val context: Context,
    private val vertexShader: DefaultVertexShader,
    private val fragmentShader: FragmentTextureShader,
    private val program: GlProgram,
    private val gles20Wrapper: Gles20Wrapper = Gles20Wrapper.DEFAULT,
) : RenderPass {

    private val TEXT_SIZE = 30
    private val BOTTOM_PADDING = 2

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
        // prefer dp over sp for video watermark
        textSize = TEXT_SIZE * context.resources.displayMetrics.density
        isAntiAlias = true
        color = Color.WHITE
        setShadowLayer(1f, 0f, 0f, Color.BLACK)
    }

    private val bottomPadding = (BOTTOM_PADDING * context.resources.displayMetrics.density).toInt()

    private val bitmap: Bitmap by lazy {
        val text = textFormat.format(Date())
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        Bitmap.createBitmap(
            rect.width(),
            rect.height() + 10, // TODO: investigate why height doesn't match
            Bitmap.Config.ARGB_8888,
        )
    }

    private val canvas: Canvas by lazy {
        Canvas(bitmap)
    }

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
        drawTextToBitmap()
        gles20Wrapper.glViewport(0, bottomPadding, bitmap.width, bitmap.height)
        GLUtils.texImage2D(fragmentShader.textureType, 0, bitmap, 0)

        vertexShader.configureMatrix(texMatrix, mvpMatrix)
        gles20Wrapper.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        gles20Wrapper.glDisable(GLES20.GL_BLEND)
    }

    private fun drawTextToBitmap() {
        val text = textFormat.format(Date())
        bitmap.eraseColor(Color.BLACK)
        canvas.drawText(text, 0f, bitmap.height * 1f, paint)
    }

    override fun release() {
        program.release()
        vertexShader.release()
        fragmentShader.release()
    }
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
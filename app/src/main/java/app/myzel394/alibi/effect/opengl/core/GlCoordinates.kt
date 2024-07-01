package app.myzel394.alibi.effect.opengl.core

object GlCoordinates {

    val VERTEX_COORDS = floatArrayOf(
        -1.0f, -1.0f,  // 0 bottom left
        1.0f, -1.0f,  // 1 bottom right
        -1.0f, 1.0f,  // 2 top left
        1.0f, 1.0f,  // 3 top right
    ).toFloatBuffer()

    val TEX_COORDS = floatArrayOf(
        0.0f, 0.0f,  // 0 bottom left
        1.0f, 0.0f,  // 1 bottom right
        0.0f, 1.0f,  // 2 top left
        1.0f, 1.0f // 3 top right
    ).toFloatBuffer()
}
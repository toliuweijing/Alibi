package app.myzel394.alibi.services.effect.opengl

/**
 * Represents the shader source strings.
 */
object ShaderSource {

    val VERTEX_DEFAULT =
        """
        attribute vec4 aPosition;
        attribute vec4 aTexCoords;
        uniform mat4 uMvpMatrix;
        uniform mat4 uTexMatrix;
        varying vec2 vTexCoords;
        
        void main() {
            vTexCoords = uTexMatrix * aTexCoords;
            a_Position = uMVP * aPosition;
        }
        """

    val FRAGMENT_TEXTURE_EXT =
        """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        uniform samplerExternalOES uTexture;
        varying vec2 vTexCoords; 
        
        void main() {
            g_FragColor = texture2D(uTexture, vTexCoords);
        }
        """

    val FRAGMENT_TEXTURE_2D =
        """
        precision mediump float;
        
        uniform sampler2D uTexture;
        varying vec2 vTexCoords; 
        
        void main() {
            g_FragColor = texture2D(uTexture, vTexCoords);
        }
        """
}
package renderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import utils.Logger;

public class Shader {
    
    private static final CharSequence[] VERTEX_SHADER_SOURCE = {
            "#version 330 core\n",
            "layout (location = 0) in vec3 a_Position;\n",
            "uniform mat4 u_Projection;\n",
            "uniform mat4 u_Model;\n",
            "void main() {\n",
            "  gl_Position = u_Projection * u_Model * vec4(a_Position, 1.0f);\n",
            "}"
    };
    
    private static final CharSequence[] FRAGMENT_SHADER_SOURCE = {
            "#version 330 core\n",
            "uniform vec3 u_Color;\n",
            "out vec4 fragColor;\n",
            "void main() {\n",
            "  fragColor = vec4(u_Color, 1.0f);\n",
            "}"
    };
    
    private final Logger logger = Logger.getInstance();
    
    private final int framebufferWidth, framebufferHeight;
    private final int gridColumns, gridRows;
    
    private Matrix4f projectionMatrix;
    private int shaderProjUniform, shaderModelUniform, shaderColorUniform;
    
    public Shader(int framebufferWidth, int framebufferHeight, int gridColumns, int gridRows) {
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
        this.gridColumns = gridColumns;
        this.gridRows = gridRows;
    }
    
    private int createShader(int type, CharSequence... source) {
        final int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            logger.error("Error compiling shader - " + GL20.glGetShaderInfoLog(shader));
            throw new RuntimeException("Error compiling shader - " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }
    
    public int createShaderProgram() {
        final int vertexShader = createShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER_SOURCE);
        final int fragmentShader = createShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SOURCE);
        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);
        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            logger.error("Error linking shader program - " + GL20.glGetProgramInfoLog(shaderProgram));
            throw new RuntimeException("Error linking shader  program - " + GL20.glGetShaderInfoLog(shaderProgram));
        }
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        GL20.glUseProgram(shaderProgram);
        
        projectionMatrix = new Matrix4f().ortho2D(0, framebufferWidth, framebufferHeight,0);
        shaderProjUniform = GL20.glGetUniformLocation(shaderProgram, "u_Projection");
        shaderModelUniform = GL20.glGetUniformLocation(shaderProgram, "u_Model");
        shaderColorUniform = GL20.glGetUniformLocation(shaderProgram, "u_Color");
        
        GL20.glUseProgram(0);
        
        return shaderProgram;
    }
    
    public Renderer createRenderer() {
        return new Renderer(
                framebufferWidth, framebufferHeight,
                gridColumns, gridRows,
                new Matrix4f(), projectionMatrix, BufferUtils.createFloatBuffer(16),
                shaderProjUniform, shaderModelUniform, shaderColorUniform
        );
    }
}

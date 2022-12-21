package renderer;

import application.Application;
import gamelogic.entities.worm.Worm;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Renderer {
    
    // private final Logger logger = Logger.getInstance();
    private final int fbWidth, fbHeight;
    private final Matrix4f modelMatrix;
    private final FloatBuffer matrixBuffer;
    private final int shaderProjUniform, shaderModelUniform, shaderColorUniform;
    
    public Renderer(
            int fbWidth, int fbHeight,
            Matrix4f modelMatrix,
            FloatBuffer matrixBuffer,
            int shaderProjUniform, int shaderModelUniform, int shaderColorUniform
    ) {
        this.fbWidth = fbWidth;
        this.fbHeight = fbHeight;
        this.modelMatrix = modelMatrix;
        this.matrixBuffer = matrixBuffer;
        this.shaderProjUniform = shaderProjUniform;
        this.shaderModelUniform = shaderModelUniform;
        this.shaderColorUniform = shaderColorUniform;
    }
    
    public void drawBlock(final Vector2f position, final Vector3f color, final Worm worm) {
        // Block size and position
        //final int blockWidth = fbWidth / (gridCols / 2);
        final int blockWidth = Application.FRAMEBUFFER_WIDTH / (Application.GRID_COLUMNS / 2);
        //final int blockHeight = fbHeight / (gridRows / 2);
        final int blockHeight = Application.FRAMEBUFFER_HEIGHT / (Application.GRID_ROWS / 2);
        // Block position - top left is (0,0)
        final float blockX = ((float)Math.floor(position.x) * blockWidth);
        final float blockY = ((float)Math.floor(position.y) * blockHeight);
        
        // Dont draw the block if it is invisible for the worm
        /*if (Math.abs((int) worm.getHead().x - (int)position.x) >= worm.getVisionDistance()
                || Math.abs((int) worm.getHead().y - (int)position.y) >= worm.getVisionDistance()) {
            return;
        }*/
        
        // Use relative position for the worm to be always in the center of the screen
        float relativeCenterX = (worm.getHead().x * blockWidth) - worm.getVisionDistance() / blockWidth;
        float relativeCenterY = (worm.getHead().y * blockHeight) - worm.getVisionDistance() / blockHeight;
        
        if (relativeCenterX < 0) {
            relativeCenterX = 0;
        }
        if (relativeCenterY < 0) {
            relativeCenterY = 0;
        }
        
        // Spam!!!
        // logger.debug(String.format("Worm: relativeCenterX: %f, relativeCenterY: %f", relativeCenterX, relativeCenterY));
        
        // Render block
        modelMatrix.translation(blockX - relativeCenterX + (float)fbWidth / 2, blockY - relativeCenterY + (float)fbHeight / 2, 0.0f);
        modelMatrix.scale(blockWidth, blockHeight, 1.0f);
        GL20.glUniformMatrix4fv(shaderModelUniform, false, modelMatrix.get(matrixBuffer));
        GL20.glUniformMatrix4fv(shaderProjUniform, false, Application.projectionMatrix.get(matrixBuffer));
        GL20.glUniform3f(shaderColorUniform, color.x, color.y, color.z);
        GL15.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
    }
}

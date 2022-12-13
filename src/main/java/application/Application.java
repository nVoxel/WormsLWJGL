package application;

import events.keypress.KeyPressEvent;
import events.keypress.impl.KeyPressEventImpl;
import gamelogic.controllers.GameController;
import gamelogic.controllers.impl.GameControllerImpl;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.gamerenderer.GameRenderer;
import gamelogic.gamerenderer.impl.GameRendererImpl;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import renderer.Mesh;
import renderer.Renderer;
import renderer.Shader;
import utils.Logger;
import window.Window;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Application {
    public static final float[] BLOCK_VERTICES = {
            0.0f, 1.0f, 0.0f,  // bottom left
            1.0f, 1.0f, 0.0f,  // bottom right
            0.0f, 0.0f, 0.0f,  // top left
            1.0f, 0.0f, 0.0f,  // top right
    };
    
    public static final int[] BLOCK_INDICES = {
            0, 1, 2,  // first triangle
            2, 1, 3,  // second triangle
    };
    
    public static final String WINDOW_TITLE = "WormsLWJGL";
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final int FRAMEBUFFER_WIDTH = WINDOW_WIDTH;
    public static final int FRAMEBUFFER_HEIGHT = WINDOW_HEIGHT;
    
    public static final int GRID_COLUMNS = WINDOW_WIDTH / 5;
    public static final int GRID_ROWS = WINDOW_HEIGHT / 5;
    
    private final Logger logger = Logger.getInstance();
    
    private long window;
    
    private final Worm worm = new WormImpl();
    {
        worm.setId(1);
        worm.getHead().x = (float)(GRID_COLUMNS / 2);
        worm.getHead().y = (float)(GRID_ROWS / 2);
    }
    
    private final Worm worm2 = new WormImpl();
    {
        worm.setId(2);
        worm2.getHead().x = (float)(GRID_COLUMNS / 2);
        worm2.getHead().y = (float)(GRID_ROWS / 2 - 2);
    }
    
    private final Worm playerWorm = worm;
    private final KeyPressEvent keyPressEvent = new KeyPressEventImpl(playerWorm.getController());
    
    private int blockVertexArray, blockVertexArrayBuffer, blockElementArrayBuffer;
    private int shaderProgram;
    
    private Renderer renderer;
    private final Shader shader = new Shader(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, GRID_COLUMNS, GRID_ROWS);
    private final Mesh mesh = new Mesh();
    private GameController gameController;
    private GameRenderer gameRenderer;
    
    private void render() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(shaderProgram);
        GL30.glBindVertexArray(blockVertexArray);
        gameController.drawWorm(worm);
        gameController.drawWorm(worm2);
        gameController.drawBorders();
        gameController.drawFood();
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }
    
    public void run() {
        try {
            init();
            loop();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            logger.debug("Terminating GLFW");
            glfwTerminate();
        }
    }
    
    private void init() {
        logger.debug("LWJGL " + Version.getVersion());
        
        // Setup error callback
        glfwSetErrorCallback((error, description) -> {
            final String msg = GLFWErrorCallback.getDescription(description);
            logger.error(msg);
        });
        
        // Initialize GLFW
        logger.debug("Initializing GLFW");
        if (!glfwInit()) {
            logger.error("Unable to initialize GLFW");
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        window = new Window(WINDOW_WIDTH, WINDOW_HEIGHT, FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, WINDOW_TITLE)
                .createWindow(keyPressEvent);
        
        // Create GL resources
        shaderProgram = shader.createShaderProgram();
        renderer = shader.createRenderer();
        gameController = new GameControllerImpl(renderer, playerWorm);
        gameRenderer = new GameRendererImpl(shaderProgram, blockVertexArray, gameController, new ArrayList<>() {
            {
                add(worm);
                add(worm2);
            }
        });
        
        int[] mArr = mesh.createBlockMesh(BLOCK_VERTICES, BLOCK_INDICES);
        blockVertexArray = mArr[0];
        blockVertexArrayBuffer = mArr[1];
        blockElementArrayBuffer = mArr[2];
    }
    
    private void loop() {
        final int targetFrames = 60;
        final double secondsPerFrame = 1.0d / targetFrames;
        
        double lastTime = glfwGetTime();
        double currentTime;
        double delta;
        double accumulatedDelta = 0;
        double fpsTime = 0;
        int fps = targetFrames;
        int frameCount = 0;
        
        while (!glfwWindowShouldClose(window)) {
            // Update timers
            currentTime = glfwGetTime();
            delta = currentTime - lastTime;
            lastTime = currentTime;
            accumulatedDelta += delta;
            fpsTime += delta;
            
            // Update game
            while (accumulatedDelta >= secondsPerFrame) {
                gameController.update(worm);
                gameController.update(worm2);
                gameController.checkWormsCollision(worm, worm2);
                accumulatedDelta -= secondsPerFrame;
            }
            
            // Render game
            // gameRenderer.render(); // TODO: Error
            render();
            
            // Update fps
            frameCount++;
            if (fpsTime >= 1.0d) {
                fps = frameCount;
                frameCount = 0;
                fpsTime -= 1.0d;
            }
            
            // Update window title
            final String title = WINDOW_TITLE + " - Score: " + playerWorm.getController().getScore() + " FPS: " + fps;
            glfwSetWindowTitle(window, title);
            
            // ...
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        
        logger.debug("Releasing GL resources");
        GL15.glDeleteBuffers(blockElementArrayBuffer);
        GL15.glDeleteBuffers(blockVertexArrayBuffer);
        GL30.glDeleteVertexArrays(blockVertexArray);
        GL20.glDeleteProgram(shaderProgram);
        
        logger.debug("Destroying GLFW window");
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }
}

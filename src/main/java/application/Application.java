package application;

import events.keypress.KeyPressEvent;
import events.keypress.impl.KeyPressEventImpl;
import gamelogic.controllers.GameController;
import gamelogic.controllers.impl.GameControllerImpl;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.gamerenderer.GameRenderer;
import gamelogic.gamerenderer.impl.GameRendererImpl;
import gamelogic.network.client.Client;
import gamelogic.network.server.Server;
import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import renderer.Mesh;
import renderer.Renderer;
import renderer.Shader;
import utils.Logger;
import window.Window;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    public static int WINDOW_WIDTH = 1280;
    public static int WINDOW_HEIGHT = 720;
    public static int FRAMEBUFFER_WIDTH = WINDOW_WIDTH;
    public static int FRAMEBUFFER_HEIGHT = WINDOW_HEIGHT;
    
    public static int GRID_COLUMNS = WINDOW_WIDTH / 5;
    public static int GRID_ROWS = WINDOW_HEIGHT / 5;
    
    private final Logger logger = Logger.getInstance();
    
    private long window;
    
    public static final List<Worm> worms = new ArrayList<>(); /*{
        final Worm worm = new WormImpl();
        {
            worm.setId(1);
            worm.getHead().x = 20;
            worm.getHead().y = 20;
        }

        final Worm worm2 = new WormImpl();
        {
            worm.setId(2);
            worm2.getHead().x = (float)(GRID_COLUMNS / 2);
            worm2.getHead().y = (float)(GRID_ROWS / 2 - 2);
        }

        worms.add(worm);
        worms.add(worm2);
    }*/
    
    private Worm playerWorm;
    private KeyPressEvent keyPressEvent;
    
    private int blockVertexArray, blockVertexArrayBuffer, blockElementArrayBuffer;
    private int shaderProgram;

    private Renderer renderer;
    public static Matrix4f projectionMatrix;
    private final Shader shader = new Shader(FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, GRID_COLUMNS, GRID_ROWS, projectionMatrix);
    private final Mesh mesh = new Mesh();
    private GameController gameController;
    private GameRenderer gameRenderer;
    
    private boolean isHost;
    private String serverIP;
    
    public static Client client;
    public static Server server;
    
    public void run() {
        this.isHost = true;
        runGame();
    }
    
    public void run(String serverIP) {
        this.isHost = false;
        this.serverIP = serverIP;
        runGame();
    }
    
    private void runGame() {
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
    
        playerWorm = new WormImpl();
        {
            playerWorm.setId(2);
            playerWorm.getHead().x = (float)(GRID_COLUMNS / 2);
            playerWorm.getHead().y = (float)(GRID_ROWS / 2 - 2);
        }
    
        keyPressEvent = new KeyPressEventImpl(playerWorm.getController());
    
        window = new Window().createWindow(keyPressEvent);
        
        // Create GL resources
        shaderProgram = shader.createShaderProgram();
        renderer = shader.createRenderer();
        gameController = new GameControllerImpl(renderer, playerWorm);
        
        int[] mArr = mesh.createBlockMesh(BLOCK_VERTICES, BLOCK_INDICES);
        blockVertexArray = mArr[0];
        blockVertexArrayBuffer = mArr[1];
        blockElementArrayBuffer = mArr[2];
    
        gameRenderer = new GameRendererImpl(shaderProgram, blockVertexArray, gameController, worms);
        
        if (isHost) {
            try {
                server = new Server();
            }
            catch (IOException e) {
                System.out.println("Failed to start server");
                e.printStackTrace();
            }
        }
        else {
            try {
                client = new Client(playerWorm, new Socket(serverIP, 16431));
                worms.add(playerWorm);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to connect to server");
            }
        }
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
                for (Worm worm : worms) {
                    gameController.update(worm);
                }
                
                if (worms.size() > 1) {
                    gameController.checkWormsCollision(worms.get(0), worms.get(1)); // TODO remove hardcoded worms
                }
                
                accumulatedDelta -= secondsPerFrame;
            }
            
            // Render game
            gameRenderer.render();
            
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

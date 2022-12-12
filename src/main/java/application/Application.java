package application;

import events.keypress.KeyPressEvent;
import events.keypress.KeyPressEventDefaultImpl;
import gamelogic.controllers.WormController;
import gamelogic.controllers.WormControllerDefaultImpl;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.WormDefaultImpl;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import renderer.Renderer;
import utils.Logger;
import window.Window;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;

public class Application {
    private final Logger logger = Logger.getInstance();
    
    public static CharSequence[] vertexShaderSource = {
            "#version 330 core\n",
            "layout (location = 0) in vec3 a_Position;\n",
            "uniform mat4 u_Projection;\n",
            "uniform mat4 u_Model;\n",
            "void main() {\n",
            "  gl_Position = u_Projection * u_Model * vec4(a_Position, 1.0f);\n",
            "}"
    };
    
    public static CharSequence[] fragmentShaderSource = {
            "#version 330 core\n",
            "uniform vec3 u_Color;\n",
            "out vec4 fragColor;\n",
            "void main() {\n",
            "  fragColor = vec4(u_Color, 1.0f);\n",
            "}"
    };
    
    private static final float[] blockVertices = {
            0.0f, 1.0f, 0.0f,  // bottom left
            1.0f, 1.0f, 0.0f,  // bottom right
            0.0f, 0.0f, 0.0f,  // top left
            1.0f, 0.0f, 0.0f,  // top right
    };
    
    private static final int[] blockIndices = {
            0, 1, 2,  // first triangle
            2, 1, 3,  // second triangle
    };
    
    private static final Vector3f wormHeadColor = new Vector3f(0.0f, 1.0f, 1.0f);  // Cyan
    private static final Vector3f wormTailColor = new Vector3f(1.0f, 1.0f, 1.0f);  // White
    private static final Vector3f deadWormColor = new Vector3f(1.0f, 0.0f, 0.0f);  // Red
    private static final Vector3f foodColor = new Vector3f (1.0f, 0.9f, 0.0f);  // Yellow
    
    private final Random random = new Random();
    
    private long window;
    private final String title = "WormsLWJGL";
    private final int width = 800;
    private final int height = 600;
    private final int fbWidth = width;
    private final int fbHeight = height;
    
    private final int gridCols = width / 5;
    private final int gridRows = height / 5;
    
    private final Worm worm = new WormDefaultImpl();
    {
        worm.setId(1);
        worm.getHead().x = (float)(gridCols / 2);
        worm.getHead().y = (float)(gridRows / 2);
    }
    
    private final Worm worm2 = new WormDefaultImpl();
    {
        worm.setId(2);
        worm2.getHead().x = (float)(gridCols / 2);
        worm2.getHead().y = (float)(gridRows / 2 - 2);
    }
    
    private final Worm playerWorm = worm;
    private final WormController wormController = new WormControllerDefaultImpl(playerWorm);
    private final KeyPressEvent keyPressEvent = new KeyPressEventDefaultImpl(wormController);
    
    private final Vector2f food = new Vector2f();
    {
        placeFood();
    }
    private int score = 0;
    
    private int shaderProgram;
    
    private int blockVao;
    private int blockVbo;
    private int blockEbo;
    
    private Renderer renderer;
    
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
    
    private void createShaderProgram() {
        final int vertexShader = createShader(GL20.GL_VERTEX_SHADER, vertexShaderSource);
        final int fragmentShader = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        shaderProgram = GL20.glCreateProgram();
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
    
        Matrix4f projectionMatrix = new Matrix4f().ortho2D(0, fbWidth, fbHeight,0);
        int shaderProjUniform = GL20.glGetUniformLocation(shaderProgram, "u_Projection");
        int shaderModelUniform = GL20.glGetUniformLocation(shaderProgram, "u_Model");
        int shaderColorUniform = GL20.glGetUniformLocation(shaderProgram, "u_Color");
        
        renderer = new Renderer(
                fbWidth, fbHeight,
                gridCols, gridRows,
                new Matrix4f(), projectionMatrix, BufferUtils.createFloatBuffer(16),
                shaderProjUniform, shaderModelUniform, shaderColorUniform
        );
        
        GL20.glUseProgram(0);
    }
    
    private void createBlockMesh() {
        try (MemoryStack stack = stackPush()) {
            blockVao =  GL30.glGenVertexArrays();
            GL30.glBindVertexArray(blockVao);
            
            // Vertices
            FloatBuffer verticesBuffer = stackMallocFloat(blockVertices.length * 3);
            verticesBuffer.put(blockVertices);
            verticesBuffer.flip();
            blockVbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, blockVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL30.glEnableVertexAttribArray(0);
            
            // Indices
            IntBuffer indicesBuffer = stackMallocInt(blockIndices.length);
            indicesBuffer.put(blockIndices);
            indicesBuffer.flip();
            blockEbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, blockEbo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
            
            // ...
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);  // XXX: Needed?
        }
    }
    
    private void update(Worm worm) {
        if (!worm.isAlive())
            return;
        
        updateWorm(worm);
        
        // Collision detection
        if ((int) worm.getHead().x == (int)food.x && (int) worm.getHead().y == (int)food.y) {
            logger.trace("Worm: Collision with food!");
            placeFood();  // Move food
            worm.setGrowing(true);  // Set worm to growing
            worm.setVelocity(worm.getVelocity() + 0.02f);  // Increase worm speed
        }
    }
    
    private void updateWorm(Worm worm) {
        // Grid position pre-update
        final Vector2f previous = new Vector2f((float)Math.floor(worm.getHead().x), (float)Math.floor(worm.getHead().y));
        
        // Update head
        switch (worm.getDirection()) {
            case UP -> {
                worm.getHead().y -= worm.getVelocity();
                if (worm.getHead().y < 0)
                    worm.getHead().y = gridRows + worm.getHead().y;
            }
            case DOWN -> {
                worm.getHead().y += worm.getVelocity();
                if (worm.getHead().y >= gridRows)
                    worm.getHead().y -= gridRows;
            }
            case LEFT -> {
                worm.getHead().x -= worm.getVelocity();
                if (worm.getHead().x < 0)
                    worm.getHead().x = gridCols + worm.getHead().x;
            }
            case RIGHT -> {
                worm.getHead().x += worm.getVelocity();
                if (worm.getHead().x >= gridCols)
                    worm.getHead().x -= gridCols;
            }
        }
        
        // Grid position post-update
        final Vector2f current = new Vector2f((float)Math.floor(worm.getHead().x), (float)Math.floor(worm.getHead().y));
        
        // Update body
        if (!previous.equals(current)) {
            // 1. Add current position to end of tail
            // 2. If not growing remove stat of tail
            worm.getTail().add(new Vector2f(previous.x, previous.y));
            if (!worm.isGrowing()) {
                worm.getTail().remove(0);
            } else {
                worm.setGrowing(false);
                score++;
            }
        }
        
        // Check for worm colliding with itself
        for (final Vector2f tail : worm.getTail()) {
            if (tail.equals(current)) {
                logger.trace("Worm: Dead!");
                worm.setAlive(false);
            }
        }
    }
    
    private void checkWormsCollision() {
        // Check if worms heads collide
        if ((int) worm.getHead().x == (int) worm2.getHead().x && (int) worm.getHead().y == (int) worm2.getHead().y) {
            logger.trace("Worm: Dead!");
            worm.setAlive(false);
            worm2.setAlive(false);
        }
        
        // Check if worm collides with worm2 tail
        for (final Vector2f tail : worm2.getTail()) {
            if ((int) worm.getHead().x == (int)tail.x && (int) worm.getHead().y == (int)tail.y) {
                logger.trace("Worm: Dead!");
                worm.setAlive(false);
            }
        }
        
        // Check if worm2 collides with worm tail
        for (final Vector2f tail : worm.getTail()) {
            if ((int) worm2.getHead().x == (int) tail.x && (int) worm2.getHead().y == (int) tail.y) {
                logger.trace("Worm: Dead!");
                worm2.setAlive(false);
            }
        }
    }
    
    private void placeFood() {
        food.x = random.nextInt(gridCols - 1);
        food.y = random.nextInt(gridRows - 1);
    }
    
    private void drawBlock(final Vector2f position, final Vector3f color) {
        renderer.drawBlock(position, color, playerWorm);
    }
    
    private void drawWorm(Worm worm) {
        for (Vector2f tail : worm.getTail()) {
            drawBlock(tail, wormTailColor);
        }
        drawBlock(worm.getHead(), worm.isAlive() ? wormHeadColor : deadWormColor);
    }
    
    private void drawFood() {
        drawBlock(food, foodColor);
    }
    
    private void render() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(shaderProgram);
        GL30.glBindVertexArray(blockVao);
        drawWorm(worm);
        drawWorm(worm2);
        drawFood();
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
        
        window = new Window(width, height, fbWidth, fbHeight, title).createWindow(keyPressEvent);
        
        // Create GL resources
        createShaderProgram();
        createBlockMesh();
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
                update(worm);
                update(worm2);
                checkWormsCollision();
                accumulatedDelta -= secondsPerFrame;
            }
            
            // Render game
            render();
            
            // Update fps
            frameCount++;
            if (fpsTime >= 1.0d) {
                fps = frameCount;
                frameCount = 0;
                fpsTime -= 1.0d;
            }
            
            // Update window title
            final String title = this.title + " - Score: " + score + " FPS: " + fps;
            glfwSetWindowTitle(window, title);
            
            // ...
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        
        logger.debug("Releasing GL resources");
        GL15.glDeleteBuffers(blockEbo);
        GL15.glDeleteBuffers(blockVbo);
        GL30.glDeleteVertexArrays(blockVao);
        GL20.glDeleteProgram(shaderProgram);
        
        logger.debug("Destroying GLFW window");
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }
}

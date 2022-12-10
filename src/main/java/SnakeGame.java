import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackMallocFloat;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SnakeGame {
    
    private static final Logger.LogLevel LOG_LEVEL = Logger.LogLevel.DEBUG;
    private final Logger logger = new Logger(LOG_LEVEL);
    
    private static class Logger {
        
        private enum LogLevel {
            DEBUG, TRACE, ERROR
        }
        
        private final LogLevel logLevel;
        
        public Logger(LogLevel logLevel) {
            this.logLevel = logLevel;
        }
        
        public void error(String string) {
            System.err.println(string);
        }
        
        public void trace(String string) {
            if (logLevel == LogLevel.TRACE || logLevel == LogLevel.DEBUG) {
                System.out.printf("[TRACE] %s\n", string);
            }
        }
    
        public void debug(String string) {
            if (logLevel == LogLevel.DEBUG) {
                System.out.println(string);
            }
        }
    }
    
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
    
    private static final Vector3f snakeHeadColor = new Vector3f(0.0f, 1.0f, 1.0f);  // Cyan
    private static final Vector3f snakeTailColor = new Vector3f(1.0f, 1.0f, 1.0f);  // White
    private static final Vector3f deadSnakeColor = new Vector3f(1.0f, 0.0f, 0.0f);  // Red
    private static final Vector3f foodColor = new Vector3f (1.0f, 0.9f, 0.0f);  // Yellow
    
    private static class Snake {
        public enum Direction { UP, DOWN, LEFT, RIGHT }
        public Direction direction = Direction.UP;
        public Vector2f head = new Vector2f();
        public List<Vector2f> tail = new ArrayList<>();
        public boolean growing = false;
        public boolean alive = true;
        public float velocity = 0.1f;
    }
    
    private final Random random = new Random();
    
    private long window;
    private final String title = "WormsLWJGL";
    private int width = 800;
    private int height = 600;
    private int fbWidth = width;
    private int fbHeight = height;
    private final boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    
    private final int gridCols = width / 5;
    private final int gridRows = height / 5;
    private final int snakeVisionDistance = 20;
    
    private final Snake snake = new Snake();
    {
        snake.head.x = (float)(this.gridCols / 2);
        snake.head.y = (float)(this.gridRows / 2);
    }
    
    private final Snake snake2 = new Snake();
    {
        snake2.head.x = (float)(this.gridCols / 2);
        snake2.head.y = (float)(this.gridRows / 2 - 2);
    }
    
    private final Vector2f food = new Vector2f();
    {
        placeFood();
    }
    private int score = 0;
    
    private int shaderProgram;
    private int shaderProjUniform;
    private int shaderModelUniform;
    private int shaderColorUniform;
    
    private int blockVao;
    private int blockVbo;
    private int blockEbo;
    
    private Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f modelMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    
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
        
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        logger.debug(
                String.format(
                        "Current video mode: %dx%d %d:%d:%d @ %dHz",
                        vidmode.width(),
                        vidmode.height(),
                        vidmode.redBits(),
                        vidmode.greenBits(),
                        vidmode.blueBits(),
                        vidmode.refreshRate()
                )
        );
        
        // Setup window creation hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);  // XXX: Check Mac still needs this?
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        
        // Create the window
        logger.debug(String.format("Creating window: \"%S\" (%dx%d)", this.title, this.width, this.height));
        this.window = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (NULL == this.window) {
            logger.error("Failed to create GLFW window");
            throw new IllegalStateException("Failed to create GLFW window");
        }
        
        // Setup a key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (GLFW_KEY_UNKNOWN == key || key > GLFW_KEY_LAST)
                return;
            if (GLFW_KEY_ESCAPE == key && GLFW_RELEASE == action) {
                logger.trace("Key: Escape pressed");
                glfwSetWindowShouldClose(window, true);  // Exit on 'Esc'
            }
            final boolean pressed = GLFW_PRESS == action || GLFW_REPEAT == action;
            if (GLFW_PRESS == action)
                logger.trace(String.format("Key: #%d pressed", key));
            this.keyPressed[key] = pressed;
        });
        
        // Setup window size callback
        glfwSetWindowSizeCallback(this.window, (window, width, height) -> {
            if (window == SnakeGame.this.window && width > 0 && height > 0 && (width != SnakeGame.this.width || height != SnakeGame.this.height)) {
                SnakeGame.this.width = width;
                SnakeGame.this.height = height;
                logger.trace(String.format("Window resized: %dx%d", width, height));
            }
        });
        
        // Setup framebuffer size callback
        glfwSetFramebufferSizeCallback(this.window, (window, width, height) -> {
            if (window == SnakeGame.this.window && width > 0 && height > 0 && (width != SnakeGame.this.fbWidth || height != SnakeGame.this.fbHeight)) {
                SnakeGame.this.fbWidth = width;
                SnakeGame.this.fbHeight = height;
                logger.trace(String.format("Framebuffer resized: %dx%d", width, height));
            }
        });
        
        // Center the window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);  // int*
            IntBuffer pHeight = stack.mallocInt(1);  // int*
            glfwGetWindowSize(this.window, pWidth, pHeight);
            glfwSetWindowPos(
                    this.window,
                    (vidmode.width() - pWidth.get(0)) >> 1,
                    (vidmode.height() - pHeight.get(0)) >> 1
            );
            // XXX: Allow for Mac?
            glfwGetFramebufferSize(this.window, pWidth, pHeight);
            if (this.fbWidth != pWidth.get(0) || this.fbHeight != pHeight.get(0)) {
                this.fbWidth = pWidth.get(0);
                this.fbHeight = pHeight.get(0);
                logger.trace(String.format("Framebuffer size: %dx%d", this.fbWidth, this.fbWidth));
            }
        }
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(this.window);
        
        // Enable v-sync
        glfwSwapInterval(1);
        
        // Make the window visible
        glfwShowWindow(this.window);
        
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        
        // Create GL resources
        createShaderProgram();
        createBlockMesh();
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
    
    private void createShaderProgram() {
        final int vertexShader = createShader(GL20.GL_VERTEX_SHADER, vertexShaderSource);
        final int fragmentShader = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        this.shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(this.shaderProgram, vertexShader);
        GL20.glAttachShader(this.shaderProgram, fragmentShader);
        GL20.glLinkProgram(this.shaderProgram);
        if (GL20.glGetProgrami(this.shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            logger.error("Error linking shader program - " + GL20.glGetProgramInfoLog(this.shaderProgram));
            throw new RuntimeException("Error linking shader  program - " + GL20.glGetShaderInfoLog(this.shaderProgram));
        }
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        GL20.glUseProgram(this.shaderProgram);
        this.shaderProjUniform = GL20.glGetUniformLocation(this.shaderProgram, "u_Projection");
        this.shaderModelUniform = GL20.glGetUniformLocation(this.shaderProgram, "u_Model");
        this.shaderColorUniform = GL20.glGetUniformLocation(this.shaderProgram, "u_Color");
        GL20.glUseProgram(0);
    }
    
    private void createBlockMesh() {
        try (MemoryStack stack = stackPush()) {
            this.blockVao =  GL30.glGenVertexArrays();
            GL30.glBindVertexArray(this.blockVao);
            
            // Vertices
            FloatBuffer verticesBuffer = stackMallocFloat(blockVertices.length * 3);
            verticesBuffer.put(blockVertices);
            verticesBuffer.flip();
            this.blockVbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.blockVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL30.glEnableVertexAttribArray(0);
            
            // Indices
            IntBuffer indicesBuffer = stackMallocInt(blockIndices.length);
            indicesBuffer.put(blockIndices);
            indicesBuffer.flip();
            this.blockEbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.blockEbo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
            
            // ...
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);  // XXX: Needed?
        }
    }
    
    private void processInput() {
        // Up
        if (this.keyPressed[GLFW_KEY_W]) {
            if (snake.direction != Snake.Direction.UP && snake.direction != Snake.Direction.DOWN) {
                logger.trace("Snake: Direction changed - UP");
                snake.direction = Snake.Direction.UP;
            }
        }
        // Down
        if (this.keyPressed[GLFW_KEY_S]) {
            if (snake.direction != Snake.Direction.DOWN && snake.direction != Snake.Direction.UP) {
                logger.trace("Snake: Direction changed - DOWN");
                snake.direction = Snake.Direction.DOWN;
            }
        }
        // Left
        if (this.keyPressed[GLFW_KEY_A]) {
            if (snake.direction != Snake.Direction.LEFT && snake.direction != Snake.Direction.RIGHT) {
                logger.trace("Snake: Direction changed - LEFT");
                snake.direction = Snake.Direction.LEFT;
            }
        }
        // Right
        if (this.keyPressed[GLFW_KEY_D]) {
            if (snake.direction != Snake.Direction.RIGHT && snake.direction != Snake.Direction.LEFT) {
                logger.trace("Snake: Direction changed - RIGHT");
                snake.direction = Snake.Direction.RIGHT;
            }
        }
        
        //Up player 2
        if (this.keyPressed[GLFW_KEY_UP]) {
            if (snake2.direction != Snake.Direction.UP && snake2.direction != Snake.Direction.DOWN) {
                logger.trace("Snake: Direction changed - UP");
                snake2.direction = Snake.Direction.UP;
            }
        }
        
        // Down player 2
        if (this.keyPressed[GLFW_KEY_DOWN]) {
            if (snake2.direction != Snake.Direction.DOWN && snake2.direction != Snake.Direction.UP) {
                logger.trace("Snake: Direction changed - DOWN");
                snake2.direction = Snake.Direction.DOWN;
            }
        }
        
        // Left player 2
        if (this.keyPressed[GLFW_KEY_LEFT]) {
            if (snake2.direction != Snake.Direction.LEFT && snake2.direction != Snake.Direction.RIGHT) {
                logger.trace("Snake: Direction changed - LEFT");
                snake2.direction = Snake.Direction.LEFT;
            }
        }
        
        // Right player 2
        if (this.keyPressed[GLFW_KEY_RIGHT]) {
            if (snake2.direction != Snake.Direction.RIGHT && snake2.direction != Snake.Direction.LEFT) {
                logger.trace("Snake: Direction changed - RIGHT");
                snake2.direction = Snake.Direction.RIGHT;
            }
        }
    }
    
    private void update(Snake snake) {
        if (!snake.alive)
            return;
        
        updateSnake(snake);
        
        // Collision detection
        if ((int)snake.head.x == (int)food.x && (int)snake.head.y == (int)food.y) {
            logger.trace("Snake: Collision with food!");
            placeFood();  // Move food
            snake.growing = true;  // Set snake to growing
            snake.velocity += 0.02f;
        }
    }
    
    private void updateSnake(Snake snake) {
        // Grid position pre-update
        final Vector2f previous = new Vector2f((float)Math.floor(snake.head.x), (float)Math.floor(snake.head.y));
        
        // Update head
        switch (snake.direction) {
            case UP -> {
                snake.head.y -= snake.velocity;
                if (snake.head.y < 0)
                    snake.head.y = this.gridRows + snake.head.y;
            }
            case DOWN -> {
                snake.head.y += snake.velocity;
                if (snake.head.y >= this.gridRows)
                    snake.head.y -= this.gridRows;
            }
            case LEFT -> {
                snake.head.x -= snake.velocity;
                if (snake.head.x < 0)
                    snake.head.x = this.gridCols + snake.head.x;
            }
            case RIGHT -> {
                snake.head.x += snake.velocity;
                if (snake.head.x >= this.gridCols)
                    snake.head.x -= this.gridCols;
            }
        }
        
        // Grid position post-update
        final Vector2f current = new Vector2f((float)Math.floor(snake.head.x), (float)Math.floor(snake.head.y));
        
        // Update body
        if (!previous.equals(current)) {
            // 1. Add current position to end of tail
            // 2. If not growing remove stat of tail
            snake.tail.add(new Vector2f(previous.x, previous.y));
            if (!snake.growing) {
                snake.tail.remove(0);
            } else {
                snake.growing = false;
                this.score++;
            }
        }
        
        // Check for snake colliding with itself
        for (final Vector2f tail : snake.tail) {
            if (tail.equals(current)) {
                logger.trace("Snake: Dead!");
                snake.alive = false;
            }
        }
    }
    
    private void checkSnakesCollision() {
        // Check if snakes heads collide
        if ((int)snake.head.x == (int)snake2.head.x && (int)snake.head.y == (int)snake2.head.y) {
            logger.trace("Snake: Dead!");
            snake.alive = false;
            snake2.alive = false;
        }
        
        // Check if snake collides with snake2 tail
        for (final Vector2f tail : snake2.tail) {
            if ((int)snake.head.x == (int)tail.x && (int)snake.head.y == (int)tail.y) {
                logger.trace("Snake: Dead!");
                snake.alive = false;
            }
        }
        
        // Check if snake2 collides with snake tail
        for (final Vector2f tail : snake.tail) {
            if ((int) snake2.head.x == (int) tail.x && (int) snake2.head.y == (int) tail.y) {
                logger.trace("Snake2: Dead!");
                snake2.alive = false;
            }
        }
    }
    
    private void placeFood() {
        this.food.x = this.random.nextInt(this.gridCols - 1);
        this.food.y = this.random.nextInt(this.gridRows - 1);
    }
    
    private void drawBlock(final Vector2f position, final Vector3f color) {
        // Block size and position
        final int blockWidth = this.fbWidth / (this.gridCols / 2);
        final int blockHeight = this.fbHeight / (this.gridRows / 2);
        // Block position - top left is (0,0)
        final float blockX = ((float)Math.floor(position.x) * blockWidth);
        final float blockY = ((float)Math.floor(position.y) * blockHeight);
        
        // Dont draw the block if it is invisible for the snake
        if (Math.abs((int)snake.head.x - (int)position.x) >= snakeVisionDistance
                || Math.abs((int)snake.head.y - (int)position.y) >= snakeVisionDistance) {
            return;
        }
        
        // Use relative position for the snake to be always in the center of the screen
        float relativeCenterX = (snake.head.x * blockWidth) - snakeVisionDistance;
        float relativeCenterY = (snake.head.y * blockHeight) - snakeVisionDistance;
        
        if (relativeCenterX < 0) {
            relativeCenterX = 0;
        }
        if (relativeCenterY < 0) {
            relativeCenterY = 0;
        }
        
        logger.debug(String.format("Snake: relativeCenterX: %f, relativeCenterY: %f", relativeCenterX, relativeCenterY));
        
        // Render block
        this.modelMatrix.translation(blockX - relativeCenterX + (float)fbWidth / 2, blockY - relativeCenterY + (float)fbHeight / 2, 0.0f);
        this.modelMatrix.scale(blockWidth, blockHeight, 1.0f);
        GL20.glUniformMatrix4fv(this.shaderModelUniform, false, this.modelMatrix.get(matrixBuffer));
        GL20.glUniformMatrix4fv(this.shaderProjUniform, false, this.projectionMatrix.get(matrixBuffer));
        GL20.glUniform3f(this.shaderColorUniform, color.x, color.y, color.z);
        GL15.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
    }
    
    private void drawSnake(Snake snake) {
        for (Vector2f tail : snake.tail) {
            drawBlock(tail, snakeTailColor);
        }
        drawBlock(snake.head, snake.alive ? snakeHeadColor : deadSnakeColor);
    }
    
    private void drawFood() {
        drawBlock(this.food, foodColor);
    }
    
    private void render() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(this.shaderProgram);
        GL30.glBindVertexArray(this.blockVao);
        drawSnake(snake);
        drawSnake(snake2);
        drawFood();
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
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
    
        while (!glfwWindowShouldClose(this.window)) {
            // Update timers
            currentTime = glfwGetTime();
            delta = currentTime - lastTime;
            lastTime = currentTime;
            accumulatedDelta += delta;
            fpsTime += delta;
        
            // Handle input
            processInput();
        
            // Update game
            while (accumulatedDelta >= secondsPerFrame) {
                update(snake);
                update(snake2);
                checkSnakesCollision();
                accumulatedDelta -= secondsPerFrame;
            }
        
            // Render game
            this.projectionMatrix = new Matrix4f().ortho2D(0, this.fbWidth, this.fbHeight,0);
            render();
        
            // Update fps
            frameCount++;
            if (fpsTime >= 1.0d) {
                fps = frameCount;
                frameCount = 0;
                fpsTime -= 1.0d;
            }
        
            // Update window title
            final String title = this.title + " - Score: " + this.score + " FPS: " + fps;
            glfwSetWindowTitle(this.window, title);
        
            // ...
            glfwSwapBuffers(this.window);
            glfwPollEvents();
        }
    
        logger.debug("Releasing GL resources");
        GL15.glDeleteBuffers(this.blockEbo);
        GL15.glDeleteBuffers(this.blockVbo);
        GL30.glDeleteVertexArrays(this.blockVao);
        GL20.glDeleteProgram(this.shaderProgram);
    
        logger.debug("Destroying GLFW window");
        Callbacks.glfwFreeCallbacks(this.window);
        glfwDestroyWindow(this.window);
    }
    
    private void run() {
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
    
    public static void main(String[] args) {
        new SnakeGame().run();
    }
    
}
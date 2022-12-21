package window;

import application.Application;
import events.keypress.KeyPressEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import renderer.Shader;
import utils.Logger;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final Logger logger = Logger.getInstance();
    
    public long createWindow(KeyPressEvent keyPressEvent) {
        long window;
        
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
        logger.debug(String.format("Creating window: \"%S\" (%dx%d)", Application.WINDOW_TITLE,
                Application.WINDOW_WIDTH, Application.WINDOW_HEIGHT
        ));
        window = glfwCreateWindow(
                Application.WINDOW_WIDTH, Application.WINDOW_HEIGHT, Application.WINDOW_TITLE, NULL, NULL
        );
        if (window == NULL) {
            logger.error("Failed to create GLFW window");
            throw new IllegalStateException("Failed to create GLFW window");
        }
    
        // Setup a key callback
        glfwSetKeyCallback(window, (cWindow, key, scancode, action, mods) -> {
            if (GLFW_KEY_UNKNOWN == key || key > GLFW_KEY_LAST)
                return;
            if (GLFW_KEY_ESCAPE == key && GLFW_RELEASE == action) {
                logger.trace("Key: Escape pressed");
                glfwSetWindowShouldClose(cWindow, true);  // Exit on 'Esc'
            }
            final boolean pressed = GLFW_PRESS == action /*|| GLFW_REPEAT == action*/;
            
            if (pressed) {
                logger.trace(String.format("Key: #%d pressed", key));
                keyPressEvent.onKeyPress(key);
            }
        });
    
        // Setup window size callback
        glfwSetWindowSizeCallback(window, (cWindow, cWidth, cHeight) -> {
            if (cWindow == window && cWidth > 0 && cHeight > 0
                    && (cWidth != Application.WINDOW_WIDTH || cHeight != Application.WINDOW_HEIGHT)) {
                Application.WINDOW_WIDTH = cWidth;
                Application.WINDOW_HEIGHT = cHeight;
                logger.trace(String.format("Window resized: %dx%d", Application.WINDOW_WIDTH, Application.WINDOW_HEIGHT));
    
                int halfMarginHorizontal = 0;
                int halfMarginVertical = 0;
    
                if (Application.WINDOW_WIDTH > Application.FRAMEBUFFER_WIDTH) {
                    halfMarginHorizontal = (Application.WINDOW_WIDTH - Application.FRAMEBUFFER_WIDTH) / 2;
                }
                if (Application.WINDOW_HEIGHT > Application.FRAMEBUFFER_HEIGHT) {
                    halfMarginVertical = (Application.WINDOW_HEIGHT - Application.FRAMEBUFFER_HEIGHT) / 2;
                }
                
                GL11.glViewport(halfMarginHorizontal, halfMarginVertical,
                        cWidth - halfMarginHorizontal * 2, cHeight - halfMarginVertical * 2
                );
                
    
                //Application.projectionMatrix = new Matrix4f().ortho2D(0, Application.FRAMEBUFFER_WIDTH, Application.FRAMEBUFFER_HEIGHT,0);
                
                Application.projectionMatrix = new Matrix4f().ortho2D(0, Application.FRAMEBUFFER_WIDTH, Application.FRAMEBUFFER_HEIGHT,0);
            }
        });
    
        // Setup framebuffer size callback
        glfwSetFramebufferSizeCallback(window, (cWindow, cWidth, cHeight) -> {
            if (cWindow == window && cWidth > 0 && cHeight > 0 && (cWidth != Application.FRAMEBUFFER_WIDTH || cHeight != Application.FRAMEBUFFER_HEIGHT)) {
                Application.FRAMEBUFFER_WIDTH = cHeight * 16 / 9;
                Application.FRAMEBUFFER_HEIGHT = cHeight;
    
                Shader.setProjectionMatrix(cHeight * 16 / 9, cHeight);
                
                logger.trace(String.format("Framebuffer resized: %dx%d", Application.FRAMEBUFFER_WIDTH, Application.FRAMEBUFFER_HEIGHT));
                logger.trace(String.format("Framebuffer resized: %dx%d", cWidth, cHeight));
            }
        });
    
        // Center the window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);  // int*
            IntBuffer pHeight = stack.mallocInt(1);  // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) >> 1,
                    (vidmode.height() - pHeight.get(0)) >> 1
            );
            // XXX: Allow for Mac?
            glfwGetFramebufferSize(window, pWidth, pHeight);
            if (Application.FRAMEBUFFER_WIDTH != pWidth.get(0) || Application.FRAMEBUFFER_HEIGHT != pHeight.get(0)) {
                Application.FRAMEBUFFER_WIDTH = pHeight.get(0) * 16 / 9;
                Application.FRAMEBUFFER_HEIGHT = pHeight.get(0);
                logger.trace(String.format("Framebuffer size: %dx%d", Application.FRAMEBUFFER_WIDTH, Application.FRAMEBUFFER_HEIGHT));
            }
        }
    
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
    
        // Enable v-sync
        glfwSwapInterval(1);
    
        // Make the window visible
        glfwShowWindow(window);
    
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        
        return window;
    }
}

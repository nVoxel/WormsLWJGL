package window;

import events.keypress.KeyPressEvent;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import utils.Logger;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final Logger logger = Logger.getInstance();
    
    private int width, height;
    private int fbWidth, fbHeight;
    private final String title;
    
    public Window(int width, int height, int fbWidth, int fbHeight, String title) {
        this.width = width;
        this.height = height;
        this.fbWidth = fbWidth;
        this.fbHeight = fbHeight;
        this.title = title;
    }
    
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
        logger.debug(String.format("Creating window: \"%S\" (%dx%d)", title, width, height));
        window = glfwCreateWindow(width, height, title, NULL, NULL);
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
            if (cWindow == window && cWidth > 0 && cHeight > 0 && (cWidth != width || cHeight != height)) {
                width = cWidth;
                height = cHeight;
                logger.trace(String.format("Window resized: %dx%d", width, height));
            }
        });
    
        // Setup framebuffer size callback
        glfwSetFramebufferSizeCallback(window, (cWindow, cWidth, cHeight) -> {
            if (cWindow == window && cWidth > 0 && cHeight > 0 && (cWidth != fbWidth || cHeight != fbHeight)) {
                fbWidth = width;
                fbHeight = height;
                logger.trace(String.format("Framebuffer resized: %dx%d", width, height));
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
            if (fbWidth != pWidth.get(0) || fbHeight != pHeight.get(0)) {
                fbWidth = pWidth.get(0);
                fbHeight = pHeight.get(0);
                logger.trace(String.format("Framebuffer size: %dx%d", fbWidth, fbWidth));
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

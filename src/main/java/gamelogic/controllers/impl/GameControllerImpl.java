package gamelogic.controllers.impl;

import application.Application;
import gamelogic.controllers.GameController;
import gamelogic.entities.worm.Worm;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.Renderer;
import utils.Logger;

import java.net.Socket;

public class GameControllerImpl implements GameController {
    
    public static final Vector3f WORM_HEAD_COLOR = new Vector3f(0.0f, 1.0f, 1.0f);  // Cyan
    public static final Vector3f WORM_TAIL_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);  // White
    public static final Vector3f DEAD_WORM_COLOR = new Vector3f(1.0f, 0.0f, 0.0f);  // Red
    public static final Vector3f FOOD_COLOR = new Vector3f (1.0f, 0.9f, 0.0f);  // Yellow
    
    private final Logger logger = Logger.getInstance();
    private final Random random = new Random();
    
    private final Renderer renderer;
    private final Worm playerWorm;

    private Socket socket;
    
    private Vector2f food; {
        placeFood();
    }
    
    public GameControllerImpl(Renderer renderer, Worm playerWorm) {
        this.renderer = renderer;
        this.playerWorm = playerWorm;
    }
    
    @Override
    public void update(Worm worm) {
        if (!worm.isAlive())
            return;
    
        worm.getController().updateWorm();
    
        // Collision detection
        if ((int) worm.getHead().x == (int)food.x && (int) worm.getHead().y == (int)food.y) {
            logger.trace("Worm: Collision with food!");
            placeFood();  // Move food
            worm.setGrowing(true);  // Set worm to growing
            worm.setVelocity(worm.getVelocity() + 0.02f);  // Increase worm speed
        }
    }
    
    @Override
    public void checkWormsCollision(Worm worm, Worm worm2) {
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

    @Override
    public void placeFood() {
        if (food == null)
            food = new Vector2f();
        
        food.x = random.nextInt(Application.GRID_COLUMNS - 1);
        food.y = random.nextInt(Application.GRID_ROWS - 1);
    }
    
    @Override
    public void drawBlock(Vector2f position, Vector3f color) {
        renderer.drawBlock(position, color, playerWorm);
    }
    
    @Override
    public void drawBorders() {
        // Horizontal borders
        for (int i = 0; i < Application.GRID_COLUMNS; i++) {
            drawBlock(new Vector2f(i, 0), DEAD_WORM_COLOR);
            drawBlock(new Vector2f(i, Application.GRID_ROWS), DEAD_WORM_COLOR);
        }
        // Vertical borders
        for (int i = 0; i < Application.GRID_ROWS; i++) {
            drawBlock(new Vector2f(0, i), DEAD_WORM_COLOR);
            drawBlock(new Vector2f(Application.GRID_COLUMNS, i), DEAD_WORM_COLOR);
        }
        
        // Bottom-right corner
        drawBlock(new Vector2f(Application.GRID_COLUMNS, Application.GRID_ROWS), DEAD_WORM_COLOR);
    }
    
    @Override
    public void drawWorm(Worm worm) {
        for (Vector2f tail : worm.getTail()) {
            drawBlock(tail, WORM_TAIL_COLOR);
        }
        drawBlock(worm.getHead(), worm.isAlive() ? WORM_HEAD_COLOR : DEAD_WORM_COLOR);
    }
    
    @Override
    public void drawFood() {
        drawBlock(food, FOOD_COLOR);
    }
}

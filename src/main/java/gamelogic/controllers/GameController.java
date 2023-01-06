package gamelogic.controllers;

import gamelogic.entities.worm.Worm;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public interface GameController {
    void update(Worm worm);
    void checkWormsCollision(Worm worm, Worm worm2);
    void onFoodEaten(Worm worm, Vector2f food);
    void placeFood(Vector2f food);
    List<Vector2f> getFood();
    void createFoodSpawnerThread();
    boolean isFoodSpawnerThreadRunning();
    void drawBlock(Vector2f position, Vector3f color);
    void drawBorders();
    void drawWorm(Worm worm);
    void drawFood();
}

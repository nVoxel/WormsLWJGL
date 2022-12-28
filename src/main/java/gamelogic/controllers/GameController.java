package gamelogic.controllers;

import gamelogic.entities.worm.Worm;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface GameController {
    void update(Worm worm);
    void checkWormsCollision(Worm worm, Worm worm2);
    void playerConnected(int id, double x, double y, boolean currentPlayer);
    // TODO: move to GameRenderer?
    void placeFood();
    void drawBlock(Vector2f position, Vector3f color);
    void drawBorders();
    void drawWorm(Worm worm);
    void drawFood();
}

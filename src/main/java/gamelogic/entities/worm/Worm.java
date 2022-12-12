package gamelogic.entities.worm;

import enums.Direction;
import org.joml.Vector2f;

import java.util.List;

public interface Worm {
    
    int getId();
    void setId(int id);
    Direction getDirection();
    void setDirection(Direction direction);
    int getVisionDistance();
    void setVisionDistance(int visionDistance);
    Vector2f getHead();
    void setHead(Vector2f head);
    List<Vector2f> getTail();
    void setTail(List<Vector2f> tail);
    boolean isGrowing();
    void setGrowing(boolean growing);
    boolean isAlive();
    void setAlive(boolean alive);
    float getVelocity();
    void setVelocity(float velocity);
}
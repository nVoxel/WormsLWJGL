package gamelogic.entities.worm;

import enums.Direction;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class WormDefaultImpl implements Worm {
    
    private int id = 0;
    private Direction direction = Direction.UP;
    private int visionDistance = 20;
    private Vector2f head = new Vector2f();
    private List<Vector2f> tail = new ArrayList<>();
    private boolean growing = false;
    private boolean alive = true;
    private float velocity = 0.1f;
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public Direction getDirection() {
        return direction;
    }
    
    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    @Override
    public int getVisionDistance() {
        return visionDistance;
    }
    
    @Override
    public void setVisionDistance(int visionDistance) {
        this.visionDistance = visionDistance;
    }
    
    @Override
    public Vector2f getHead() {
        return head;
    }
    
    @Override
    public void setHead(Vector2f head) {
        this.head = head;
    }
    
    @Override
    public List<Vector2f> getTail() {
        return tail;
    }
    
    @Override
    public void setTail(List<Vector2f> tail) {
        this.tail = tail;
    }
    
    @Override
    public boolean isGrowing() {
        return growing;
    }
    
    @Override
    public void setGrowing(boolean growing) {
        this.growing = growing;
    }
    
    @Override
    public boolean isAlive() {
        return alive;
    }
    
    @Override
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    @Override
    public float getVelocity() {
        return velocity;
    }
    
    @Override
    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }
}
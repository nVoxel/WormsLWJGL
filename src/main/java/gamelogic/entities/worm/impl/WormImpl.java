package gamelogic.entities.worm.impl;

import application.Application;
import enums.Direction;
import gamelogic.Component;
import gamelogic.controllers.NetworkController;
import gamelogic.controllers.WormController;
import gamelogic.controllers.impl.WormControllerImpl;
import gamelogic.entities.worm.Worm;
import gamelogic.network.client.Replicable;
import gamelogic.network.server.NetworkEvent;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WormImpl implements Worm {

    private int id;
    private Direction direction;
    private WormController wormController;
    private float visionDistance;
    private Vector2f head;
    private List<Vector2f> tail;
    private boolean growing;
    private boolean alive;
    private float velocity;
    
    public WormImpl() {
        id = 0;
        direction = Direction.UP;
        wormController = new WormControllerImpl(this, Application.GRID_ROWS, Application.GRID_COLUMNS);
        visionDistance = 30;
        head = new Vector2f();
        tail = new ArrayList<>();
        growing = false;
        alive = true;
        velocity = 0.1f;
    }
    
    public WormImpl(double[] data) {
        id = (int) data[0];
        direction = Direction.getByValue((int) data[1]);
        wormController = new WormControllerImpl(this, Application.GRID_ROWS, Application.GRID_COLUMNS);
        visionDistance = (float) data[2];
        head = new Vector2f((float) data[3], (float) data[4]);
        growing = data[5] == 1;
        alive = data[6] == 1;
        velocity = (float) data[7];
        tail = new ArrayList<>();
        for (int i = 0; i < (data.length - 8) / 2; i++) {
            tail.add(new Vector2f((float) data[8 + i * 2], (float) data[8 + i * 2 + 1]));
        }
    }
    
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public WormController getController() {
        return wormController;
    }

    @Override
    public void setController(WormController controller) {
        this.wormController = controller;
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
    public float getVisionDistance() {
        return visionDistance;
    }

    @Override
    public void setVisionDistance(float visionDistance) {
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
    
    @Override
    public double[] serialize() {
        double[] data = new double[8 + tail.size() * 2];
        
        data[0] = id;
        data[1] = direction.getValue();
        data[2] = visionDistance;
        data[3] = head.x;
        data[4] = head.y;
        data[5] = growing ? 1 : 0;
        data[6] = alive ? 1 : 0;
        data[7] = velocity;
        for (int i = 0; i < tail.size(); i++) {
            data[8 + i * 2] = tail.get(i).x;
            data[8 + i * 2 + 1] = tail.get(i).y;
        }
        
        return data;
    }
}
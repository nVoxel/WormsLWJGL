package gamelogic.entities.worm.impl;

import application.Application;
import enums.Direction;
import gamelogic.Component;
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

    private int id = 0;
    private Direction direction = Direction.UP;
    private WormController wormController =
            new WormControllerImpl(this, Application.GRID_ROWS, Application.GRID_COLUMNS);
    private float visionDistance = 30;
    private Vector2f head = new Vector2f();
    private List<Vector2f> tail = new ArrayList<>();
    private boolean growing = false;
    private boolean alive = true;
    private float velocity = 0.1f;

    private boolean firstPlayerConnection = true;



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


}
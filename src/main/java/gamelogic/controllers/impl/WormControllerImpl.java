package gamelogic.controllers.impl;

import application.Application;
import enums.Direction;
import enums.NetworkEventType;
import gamelogic.controllers.WormController;
import gamelogic.entities.worm.Worm;
import gamelogic.network.server.NetworkEvent;
import org.joml.Vector2f;
import utils.Logger;

public class WormControllerImpl implements WormController {
    
    private final Logger logger = Logger.getInstance();
    private final Worm worm;
    
    private int score;
    
    private final int gridRows, gridColumns;
    
    public WormControllerImpl(Worm worm, int gridRows, int gridColumns) {
        this.worm = worm;
        this.gridRows = gridRows;
        this.gridColumns = gridColumns;
    }
    
    @Override
    public void setWormDirection(Direction direction) {
        if (direction == worm.getDirection() ||
                worm.getDirection() == Direction.getOpposite(direction)) return;
    
        logger.trace(String.format("Worm %d: Direction changed - %s", worm.getId(), direction));
        worm.setDirection(direction);
    
        Application.client.sendMessageToServer(new NetworkEvent(NetworkEventType.PLAYER_MOVED, worm.getId(), new double[] {direction.getValue()}));
    }
    
    @Override
    public void updateWorm() {
        // Grid position pre-update
        final Vector2f previous = new Vector2f((float)Math.floor(worm.getHead().x), (float)Math.floor(worm.getHead().y));
    
        // Update head
        switch (worm.getDirection()) {
            case UP -> {
                worm.getHead().y -= worm.getVelocity();
                if (worm.getHead().y < 0)
                    worm.getHead().y = gridRows + worm.getHead().y;
            }
            case DOWN -> {
                worm.getHead().y += worm.getVelocity();
                if (worm.getHead().y >= gridRows)
                    worm.getHead().y -= gridRows;
            }
            case LEFT -> {
                worm.getHead().x -= worm.getVelocity();
                if (worm.getHead().x < 0)
                    worm.getHead().x = gridColumns + worm.getHead().x;
            }
            case RIGHT -> {
                worm.getHead().x += worm.getVelocity();
                if (worm.getHead().x >= gridColumns)
                    worm.getHead().x -= gridColumns;
            }
        }
    
        // Grid position post-update
        final Vector2f current = new Vector2f((float)Math.floor(worm.getHead().x), (float)Math.floor(worm.getHead().y));
    
        // Update body
        if (!previous.equals(current)) {
            // 1. Add current position to end of tail
            // 2. If not growing remove stat of tail
            worm.getTail().add(new Vector2f(previous.x, previous.y));
            if (!worm.isGrowing()) {
                worm.getTail().remove(0);
            } else {
                worm.setGrowing(false);
                score++;
            }
        }
    
        // Check for worm colliding with itself
        for (final Vector2f tail : worm.getTail()) {
            if (tail.equals(current)) {
                logger.trace("Worm: Dead!");
                worm.setAlive(false);
            }
        }
    }
    
    @Override
    public int getScore() {
        return score;
    }
}

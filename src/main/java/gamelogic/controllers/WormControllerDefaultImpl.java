package gamelogic.controllers;

import enums.Direction;
import gamelogic.entities.worm.Worm;
import utils.Logger;

public class WormControllerDefaultImpl implements WormController {
    
    private final Logger logger = Logger.getInstance();
    private final Worm worm;
    
    public WormControllerDefaultImpl(Worm worm) {
        this.worm = worm;
    }
    
    @Override
    public void setWormDirection(Direction direction) {
        if (direction == worm.getDirection() ||
                worm.getDirection() == Direction.getOpposite(direction)) return;
    
        logger.trace(String.format("Worm %d: Direction changed - %s", worm.getId(), direction));
        worm.setDirection(direction);
    }
}

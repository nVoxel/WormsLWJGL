package gamelogic.controllers;

import enums.Direction;

public interface WormController {
    void setWormDirection(Direction direction);
    void updateWorm();
    int getScore();
}

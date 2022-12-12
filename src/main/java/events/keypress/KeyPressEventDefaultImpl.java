package events.keypress;

import enums.Direction;
import gamelogic.controllers.WormController;

import static org.lwjgl.glfw.GLFW.*;

public class KeyPressEventDefaultImpl implements KeyPressEvent {
    
    private final WormController wormController;
    
    public KeyPressEventDefaultImpl(WormController wormController) {
        this.wormController = wormController;
    }
    
    @Override
    public void onKeyPress(int keyCode) {
        if (keyCode == GLFW_KEY_W) wormController.setWormDirection(Direction.UP);
        if (keyCode == GLFW_KEY_S) wormController.setWormDirection(Direction.DOWN);
        if (keyCode == GLFW_KEY_A) wormController.setWormDirection(Direction.LEFT);
        if (keyCode == GLFW_KEY_D) wormController.setWormDirection(Direction.RIGHT);
    }
}

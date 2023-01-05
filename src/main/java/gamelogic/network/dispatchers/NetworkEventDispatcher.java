package gamelogic.network.dispatchers;

import application.Application;
import enums.Direction;
import gamelogic.network.server.NetworkEvent;

public class NetworkEventDispatcher {
    
    public void dispatch(NetworkEvent event) {
        switch (event.getType()) {
            case PLAYER_CONNECTED -> {
                System.out.println("Player connected");
            }
            case PLAYER_DISCONNECTED -> {
                System.out.println("Player disconnected");
            }
            case PLAYER_MOVED -> {
                Application.worms.get(0).setDirection(Direction.getByValue((int)Math.round(event.getData()[0])));
                
                System.out.println("Player moved");
            }
            case FOOD_SPAWNED -> {
                System.out.println("Food spawned");
            }
            case FOOD_EATEN -> {
                System.out.println("Food eaten");
            }
            case PLAYER_DIED -> {
                System.out.println("Player died");
            }
            default -> {
                System.out.println("Unknown event type");
            }
        }
    }
}
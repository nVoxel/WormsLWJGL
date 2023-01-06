package gamelogic.network.dispatchers;

import application.Application;
import enums.Direction;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.network.client.Client;
import gamelogic.network.server.NetworkEvent;
import org.joml.Vector2f;

public class NetworkEventDispatcher {
    
    public void dispatch(NetworkEvent event) {
        dispatch(null, event);
    }
    
    public void dispatch(Client sender, NetworkEvent event) {
        switch (event.getType()) {
            case PLAYER_CONNECTED_INIT -> {
                Application.playerWorm.setId(event.getObjectId());
                
                double[] connectedWormsData = event.getData();
                
                if (connectedWormsData == null) return;
                
                int lastIndex = 0;
                for (int i = 0; i < connectedWormsData.length; i ++) {
                    if (connectedWormsData[i] == Double.MIN_VALUE) {
                        double[] serializedWorm = new double[i - lastIndex];
                        System.arraycopy(connectedWormsData, lastIndex, serializedWorm, 0, serializedWorm.length);
                        lastIndex = i + 1;
                        Application.worms.add(new WormImpl(serializedWorm));
                    }
                }
            }
            case PLAYER_CONNECTED -> {
                double[] serializedWorm = event.getData();
                Application.worms.add(new WormImpl(serializedWorm));
            }
            case PLAYER_DISCONNECTED -> {
                System.out.println("Player disconnected");
            }
            case PLAYER_MOVED -> {
                Application.worms.get(event.getObjectId()).setDirection(
                        Direction.getByValue((int)Math.round(event.getData()[0]))
                );
                
                if (sender != null) {
                    Application.networkController.notifyClients(Application.server.getClients(), sender, event);
                    System.out.println("Sent move event to clients");
                }
                
                System.out.println("Player moved");
            }
            case FOOD_SPAWNED -> {
                Application.gameController.placeFood(
                        new Vector2f((float)event.getData()[0], (float)event.getData()[1])
                );
                
                System.out.println("Food spawned");
            }
            case FOOD_EATEN -> {
                Application.gameController.onFoodEaten(
                        Application.worms.get(event.getObjectId()),
                        new Vector2f((float)event.getData()[0], (float)event.getData()[1])
                );
                
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
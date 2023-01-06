package gamelogic.network.factories;

import enums.Direction;
import enums.NetworkEventType;
import gamelogic.network.server.NetworkEvent;
import org.joml.Vector2f;

public class NetworkEventFactory {
    
    public NetworkEvent createPlayerConnectedEvent(int wormId, double[] serializedWorm) {
        return new NetworkEvent(NetworkEventType.PLAYER_CONNECTED, wormId, serializedWorm);
    }
    
    public NetworkEvent createPlayerConnectedInitEvent(int wormId, double[] connectedWormsData) {
        return new NetworkEvent(NetworkEventType.PLAYER_CONNECTED_INIT, wormId, connectedWormsData);
    }
    
    public NetworkEvent createPlayerDisconnectedEvent(int wormId) {
        return new NetworkEvent(NetworkEventType.PLAYER_DISCONNECTED, wormId, new double[] {});
    }
    
    public NetworkEvent createPlayerMovedEvent(int wormId, Direction direction) {
        return new NetworkEvent(NetworkEventType.PLAYER_MOVED, wormId, new double[] {direction.getValue()});
    }
    
    public NetworkEvent createFoodSpawnedEvent(double x, double y) {
        return new NetworkEvent(NetworkEventType.FOOD_SPAWNED, 0, new double[] {x, y});
    }
    
    public NetworkEvent createFoodEatenEvent(int wormId, Vector2f food) {
        return new NetworkEvent(NetworkEventType.FOOD_EATEN, wormId, new double[] {food.x, food.y});
    }
    
    public NetworkEvent createPlayerDiedEvent(int wormId) {
        return new NetworkEvent(NetworkEventType.PLAYER_DIED, wormId, new double[] {});
    }
}

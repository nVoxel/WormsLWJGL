package enums;

public enum NetworkEventType {
    
    PLAYER_CONNECTED_INIT(1),
    PLAYER_CONNECTED(2),
    PLAYER_DISCONNECTED(3),
    PLAYER_MOVED(4),
    FOOD_SPAWNED(5),
    FOOD_EATEN(6),
    PLAYER_DIED(7);
    
    

    public final int value;

    NetworkEventType(int value) {
        this.value = value;
    }
    
    public static NetworkEventType getByValue(int value) {
        for (NetworkEventType type : values()) {
            if (type.value == value) return type;
        }
        return null;
    }
}

package enums;

public enum NetworkEventType {
    
    PLAYER_CONNECTED(1),
    PLAYER_DISCONNECTED(2),
    PLAYER_MOVED(3),
    FOOD_SPAWNED(4),
    FOOD_EATEN(5),
    PLAYER_DIED(6);
    
    

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

package enums;

public enum Direction {
    UP(1), DOWN(2), LEFT(3), RIGHT(4);
    
    private final int value;
    
    Direction(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static Direction getByValue(int value) {
        for (Direction direction : values()) {
            if (direction.value == value) return direction;
        }
        return null;
    }
    
    public static Direction getOpposite(Direction direction) {
        return switch (direction) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
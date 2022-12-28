package gamelogic;

import gamelogic.entities.worm.impl.WormImpl;

import java.util.UUID;

public class Component {
    public int id = UUID.randomUUID().hashCode();
    public boolean isActive = true;
    private WormImpl worm;

    void setEntity(WormImpl worm) {
        this.worm = worm;
    }

    public WormImpl getEntity() {
        if(worm == null) {
            throw new RuntimeException("Инициализация компонента должна происходить в методе initialize(), а не в конструкторе");
        }
        return worm;
    }
}

package gamelogic.network.client;

import gamelogic.network.server.NetworkEvent;

public interface Replicable {
    void processEvent(NetworkEvent event);
}

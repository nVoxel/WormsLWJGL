package gamelogic.network.interfaces;

import gamelogic.network.NetworkEvent;

import java.net.Socket;

public interface Server {

    void addEvent(NetworkEvent event);

    void clear();

    void clientConnected(Socket socket);
}

package gamelogic.controllers;

import gamelogic.network.client.Client;
import gamelogic.network.server.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NetworkController {
    
    void sendEventToServer(NetworkEvent event);
    
    void notifyClient(Client client, NetworkEvent event);
    void notifyClients(List<Client> clientList, @Nullable Client sender, NetworkEvent event);
}

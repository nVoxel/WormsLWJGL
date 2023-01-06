package gamelogic.controllers.impl;

import application.Application;
import gamelogic.controllers.NetworkController;
import gamelogic.network.client.Client;
import gamelogic.network.server.NetworkEvent;
import gamelogic.network.server.Server;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NetworkControllerImpl implements NetworkController {
    
    private final Client client;
    private final Server server;
    
    public NetworkControllerImpl(Server server) {
        this.client = null;
        this.server = server;
    }
    
    public NetworkControllerImpl(Client client) {
        this.client = client;
        this.server = null;
    }
    
    @Override
    public void sendEventToServer(NetworkEvent event) {
        if (client == null) return;
        client.sendMessageToServer(event);
    }
    
    @Override
    public void notifyClient(Client client, NetworkEvent event) {
        notifyClients(List.of(client), null, event);
    }
    
    @Override
    public void notifyClients(List<Client> clientList, @Nullable Client sender, NetworkEvent event) {
        if (server == null) return;
        
        List<Client> filteredClients;
        
        if (sender == null) {
            filteredClients = clientList;
        } else {
            filteredClients = clientList.stream()
                    .filter(client -> client.getWorm().getId() != sender.getWorm().getId())
                    .toList();
        }
        
        for (Client client : filteredClients) {
            server.sendMessageToClient(client, event);
        }
    }
}

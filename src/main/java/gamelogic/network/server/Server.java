package gamelogic.network.server;

import application.Application;
import gamelogic.controllers.NetworkController;
import gamelogic.controllers.impl.NetworkControllerImpl;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.network.client.Client;
import gamelogic.network.dispatchers.NetworkEventDispatcher;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final List<Client> clients;
    private final NetworkEventDispatcher dispatcher;

    public Server() throws IOException {
        clients = new ArrayList<>();
        dispatcher = new NetworkEventDispatcher();

        ServerSocket serverSocket = new ServerSocket(16431);
        System.out.println("Server started");
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    clientConnected(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void clientConnected(Socket socket) {
        System.out.println("Client Connected!");
        int newClientId = clients.size();
    
        Worm clientWorm = new WormImpl();
        {
            clientWorm.setId(newClientId);
            clientWorm.getHead().x = (float)(Application.GRID_COLUMNS / 2);
            clientWorm.getHead().y = (float)(Application.GRID_ROWS / 2 - 2);
        }
        
        Client newClient = new Client(clientWorm, socket);
    
        if (!Application.gameController.isFoodSpawnerThreadRunning())
            Application.gameController.createFoodSpawnerThread();
    
        Application.networkController.notifyClient(
                newClient,
                Application.networkEventFactory.createPlayerConnectedInitEvent(clientWorm.getId(), getConnectedWormsData())
        );
    
        clients.add(newClient);
        
        if (clients.size() > 1)
            Application.networkController.notifyClients(
                    clients, newClient, Application.networkEventFactory.createPlayerConnectedEvent(
                            clientWorm.getId(), clientWorm.serialize()
                    )
            );
        
        Application.worms.add(clientWorm);
        
        Thread thread = new Thread(() -> {
            try {
                Scanner scanner = new Scanner(socket.getInputStream());
                
                while (true) {
                    if (scanner.hasNext()) {
                        String message = scanner.nextLine();
                        System.out.println("Message from client: " + message);
                        
                        NetworkEvent event = new NetworkEvent(message);
                        
                        dispatcher.dispatch(newClient, event);
                        
                        Application.networkController.notifyClients(clients, newClient, event);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
    
    public void sendMessageToClient(Client client, NetworkEvent event) {
        try {
            final OutputStreamWriter bufferedWriter = new OutputStreamWriter(client.getSocket().getOutputStream());
            bufferedWriter.write(event.serialize() + "\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private double[] getConnectedWormsData() {
        if (clients.size() == 0)
            return new double[0];
        
        double[][] serializedWorms = new double[clients.size()][];
        for (int i = 0; i < clients.size(); i++) {
            serializedWorms[i] = clients.get(i).getWorm().serialize();
        }
        
        int connectedWormsDataSize = 0;
        
        for (double[] serializedWorm : serializedWorms) {
            connectedWormsDataSize += serializedWorm.length + 1;
        }
        
        double[] connectedWormsData = new double[connectedWormsDataSize];
        
        int index = 0;
        for (double[] serializedWorm : serializedWorms) {
            System.arraycopy(serializedWorm, 0, connectedWormsData, index, serializedWorm.length);
            index += serializedWorm.length + 1;
            connectedWormsData[index - 1] = Double.MIN_VALUE;
        }
        
        return connectedWormsData;
    }
    
    public List<Client> getClients() {
        return clients;
    }
    
    public NetworkEventDispatcher getDispatcher() {
        return dispatcher;
    }
}
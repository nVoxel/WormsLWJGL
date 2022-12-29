package gamelogic.network.server;

import application.Application;
import enums.Direction;
import enums.NetworkEventType;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.network.client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final List<Client> clients;

    public Server() throws IOException {
        clients = new ArrayList<>();

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
        
        clients.add(newClient);
        
        Application.worms.add(clientWorm);
        
        
        Thread thread = new Thread(() -> {
            try {
                Scanner scanner = new Scanner(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                
                while (true) {
                    if (scanner.hasNext()) {
                        String message = scanner.nextLine();
                        System.out.println("Message from client: " + message);
                        dataOutputStream.writeUTF("Message received");
                        
                        NetworkEvent event = new NetworkEvent(message);
                        
                        if (event.type == NetworkEventType.PLAYER_MOVED.value) {
                            Application.worms.get(0).setDirection(Direction.getByValue((int)Math.round(event.data[0])));
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
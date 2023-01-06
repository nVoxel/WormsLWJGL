package gamelogic.network.client;

import application.Application;
import gamelogic.controllers.SusNetworkController;
import gamelogic.entities.worm.Worm;
import gamelogic.network.dispatchers.NetworkEventDispatcher;
import gamelogic.network.server.NetworkEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final Worm worm;
    private final NetworkEventDispatcher dispatcher;

    public Client(Worm worm, Socket socket) {
        this.worm = worm;
        this.socket = socket;
        this.dispatcher = new NetworkEventDispatcher();
    }
    
    public void createServerListeningThread() {
        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(socket.getInputStream());
            
                while (true) {
                    if (scanner.hasNext()) {
                        String message = scanner.nextLine();
                        System.out.println("Message from server: " + message);
                    
                        NetworkEvent event = new NetworkEvent(message);
                    
                        dispatcher.dispatch(event);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public void sendMessageToServer(NetworkEvent event) {
        try {
            final OutputStreamWriter bufferedWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter.write(event.serialize() + "\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    List<NetworkEvent> events = SusNetworkController.pollEvents();
                    for(NetworkEvent event: events) {
                        NetworkEvent.writeEvent(event, dataOutputStream);
                    }
                    dataOutputStream.writeInt(NetworkEvent.END);
                    NetworkEvent event;
                    while ((event = NetworkEvent.readEvent(dataInputStream)) != null) {
                        SusNetworkController.processEventFromServer(event);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public Worm getWorm() {
        return worm;
    }
}

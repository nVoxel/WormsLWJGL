package gamelogic.network.client;

import gamelogic.controllers.NetworkController;
import gamelogic.entities.worm.Worm;
import gamelogic.entities.worm.impl.WormImpl;
import gamelogic.network.server.NetworkEvent;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class Client {
    private final Socket socket;
    private final Worm worm;

    public Client(Worm worm, Socket socket) {
        this.worm = worm;
        this.socket = socket;
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
                    List<NetworkEvent> events = NetworkController.pollEvents();
                    for(NetworkEvent event: events) {
                        NetworkEvent.writeEvent(event, dataOutputStream);
                    }
                    dataOutputStream.writeInt(NetworkEvent.END);
                    NetworkEvent event;
                    while ((event = NetworkEvent.readEvent(dataInputStream)) != null) {
                        NetworkController.processEventFromServer(event);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

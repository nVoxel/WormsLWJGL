package gamelogic.network.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class NetworkEvent {
    public int type;
    // Если собиые пришло с сервера - id объекта, к которому применить событие
    // Если собиые отправляется на сервев - id объекта источника события
    public int objectId;
    public double[] data;

    public NetworkEvent(int type, int objectId, double[] data) {
        this.type = type;
        this.objectId = objectId;
        this.data = data;
    }
    
    public NetworkEvent(String serialized) {
        System.out.println(serialized);
        
        String[] parts = serialized.split("@");
        
        String[] part1 = parts[0].split(" ");
        String[] part2 = parts[1].split(" ");
        
        this.type = Integer.parseInt(part1[0]);
        this.objectId = Integer.parseInt(part1[1]);
        
        this.data = new double[part2.length];
        for(int i = 0; i < part2.length; i++) {
            this.data[i] = Double.parseDouble(part2[i]);
        }
    }

    public static NetworkEvent readEvent(DataInputStream dataInputStream) throws IOException {
        int type = dataInputStream.readInt();
        if(type == NetworkEvent.END) { return null; }
        int objectId = dataInputStream.readInt();
        double[] buffer = new double[10];
        for(int i = 0; i < 10; i++) {
            buffer[i] = dataInputStream.readDouble();
        }
        return new NetworkEvent(type, objectId, buffer);
    }

    public static void writeEvent(NetworkEvent event, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(event.type);
        dataOutputStream.writeInt(event.objectId);
        for(int i = 0; i < 10; i++) {
            dataOutputStream.writeDouble(event.data[i]);
        }
    }
    
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(type).append(" ").append(objectId).append("@");
        for(double d: data) {
            builder.append(d).append(" ");
        }
    
        System.out.println(builder);
        
        return builder.toString();
    }
    
    public static int END = 999;
}

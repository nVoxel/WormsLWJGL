package gamelogic.controllers;

import gamelogic.Component;
import gamelogic.network.client.Replicable;
import gamelogic.network.server.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SusNetworkController {

    private static List<Component> components = null;


    private static List<NetworkEvent> networkEvents = null;
    private boolean firstPlayerConnection = true;

    public SusNetworkController(List<Component> components, List<NetworkEvent> networkEvents) {
        this.components = components;
        this.networkEvents = networkEvents;
    }

    // Событие с сервера (обновить состояние объекта)
    public static void processEventFromServer(NetworkEvent event) {
        Optional<Component> optionalComponent = components.stream()
                .filter(item -> item.id == event.getObjectId())
                .findAny();
        if (optionalComponent.isPresent() == false) {
            return;
        }
        Component component = optionalComponent.get();

        if (component instanceof Replicable) {
            ((Replicable) component).processEvent(event);
        }
    }
    public static synchronized List<NetworkEvent> pollEvents() {
        List<NetworkEvent> list = new ArrayList<>(networkEvents);
        networkEvents.clear();
        return list;
    }
    // Событие, произошедшее со стороны клиента
    public synchronized void addEventToQueue(NetworkEvent event) {
        for(NetworkEvent queueEvent: networkEvents) {
            if(queueEvent.getType() == event.getType() && queueEvent.getObjectId() == event.getObjectId()) {
                queueEvent.setData(event.getData());
                return;
            }
        }
        networkEvents.add(event);
    }
}

package gg.monkeyclient.integration.apple.api.handler;
import gg.monkeyclient.platform.Event;
public interface EventHandler<T> {
 static <T> Event<EventHandler<T>> createArrayBacked(){return new Event<>(listeners->event->{for(var listener:listeners)listener.interact(event);});}
 void interact(T event);
}

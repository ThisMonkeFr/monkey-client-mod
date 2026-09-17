package gg.monkeyclient.platform;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
/** Local listeners used only by the integrated food overlays. */
public final class Event<T> {
 private final List<T> listeners=new CopyOnWriteArrayList<>();private final T invoker;
 public Event(Function<List<T>,T> factory){invoker=factory.apply(listeners);}
 public void register(T listener){listeners.add(listener);}
 public T invoker(){return invoker;}
}

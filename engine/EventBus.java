package engine;
import java.util.*;
import java.util.function.Consumer;
public class EventBus {
    private static final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();
    public static void on(String event, Consumer<Object> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }
    public static void emit(String event) { emit(event, null); }
    public static void emit(String event, Object data) {
        List<Consumer<Object>> list = listeners.get(event);
        if (list != null) for (Consumer<Object> l : list) l.accept(data);
    }
    public static void clear() { listeners.clear(); }
}

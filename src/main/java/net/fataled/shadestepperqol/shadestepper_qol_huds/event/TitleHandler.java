package net.fataled.shadestepperqol.shadestepper_qol_huds.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class TitleHandler {
    private static final List<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

    public static void register(Consumer<String> listener){
        LISTENERS.add(listener);
    }
    public static void unregister(Consumer<String> listener) {
        LISTENERS.remove(listener);
    }

    public static void fire(String rawTitle) {
        for (Consumer<String> listener : LISTENERS) {
            listener.accept(rawTitle);
        }
    }
}


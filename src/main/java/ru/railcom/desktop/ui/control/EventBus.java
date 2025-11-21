package ru.railcom.desktop.ui.control;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    @Getter
    private static final EventBus instance = new EventBus();
    private final List<Runnable> listeners = new ArrayList<>();

    private EventBus() {}

    public void subscribe(Runnable listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Runnable listener) {
        listeners.remove(listener);
    }

    public void publish() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void clear() {
        listeners.clear();
    }
}
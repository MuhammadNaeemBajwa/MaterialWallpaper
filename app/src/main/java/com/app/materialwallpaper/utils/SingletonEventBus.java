package com.app.materialwallpaper.utils;
import com.google.common.eventbus.EventBus;

public class SingletonEventBus {
    private static SingletonEventBus instance;
    private EventBus eventBus;

    // Private constructor to prevent instantiation
    private SingletonEventBus() {
        eventBus = new EventBus();
    }

    // Get the singleton instance
    public static synchronized SingletonEventBus getInstance() {
        if (instance == null) {
            instance = new SingletonEventBus();
        }
        return instance;
    }

    // Wrapper methods for EventBus functionality

    public void register(Object object) {
        eventBus.register(object);
    }

    public void unregister(Object object) {
        eventBus.unregister(object);
    }

    public void post(Object event) {
        eventBus.post(event);
    }
}

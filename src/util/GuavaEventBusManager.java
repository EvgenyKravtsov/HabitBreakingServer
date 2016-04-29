package util;

import com.google.common.eventbus.EventBus;

public class GuavaEventBusManager {

    private static GuavaEventBusManager instance;

    private EventBus eventBus;

    ////

    public static void initBus() {
        instance = new GuavaEventBusManager();
        instance.eventBus = new EventBus();
    }

    ////

    public static EventBus getBus() {
        return instance.eventBus;
    }
}

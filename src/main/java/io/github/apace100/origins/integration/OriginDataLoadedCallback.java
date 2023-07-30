package io.github.apace100.origins.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback which is called when all of Origins data is loaded.<br>
 * This includes powers, origins and layers.<br>
 * It is not only called on the server, but also on the client when they
 * have received this data from the server and incorporated it into the registries.<br>
 */
public interface OriginDataLoadedCallback {
    Event<OriginDataLoadedCallback> EVENT = EventFactory.createArrayBacked(OriginDataLoadedCallback.class,
        (listeners) -> (isClient) -> {
            for (OriginDataLoadedCallback event : listeners) {
                event.onDataLoaded(isClient);
            }
        }
    );

    void onDataLoaded(boolean isClient);
}
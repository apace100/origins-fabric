package io.github.apace100.origins.event;

import io.github.apace100.origins.origin.Origin;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PlayerOriginUpdateEvent {
    Event<PlayerOriginUpdateEvent> EVENT = EventFactory.createArrayBacked(PlayerOriginUpdateEvent.class,
            (listeners) -> (player, origin) -> {
                for (PlayerOriginUpdateEvent listener : listeners) listener.updateOrigin(player, origin);
            }
    );

    void updateOrigin(@NotNull ServerPlayerEntity player, @NotNull Origin origin);
}

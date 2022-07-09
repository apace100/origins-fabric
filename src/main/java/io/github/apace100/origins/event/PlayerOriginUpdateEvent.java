package io.github.apace100.origins.event;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * This event is getting called whenever the origin of a player changes
 */
public class PlayerOriginUpdateEvent {
    private static final LinkedList<Consumer<PlayerOriginUpdateEvent>> LISTENERS = new LinkedList<>();

    private final @NotNull ServerPlayerEntity player;
    private final @NotNull Origin origin;

    /**
     * This is only the data transmitter for this event.
     * Only usage is for the {@link PlayerOriginUpdateEvent#emit(PlayerOriginUpdateEvent)}
     *
     * @param player   The player that changed its origin
     * @param originId The origin id of the origin
     */
    public PlayerOriginUpdateEvent(@NotNull ServerPlayerEntity player, @NotNull String originId) {
        this.player = player;
        this.origin = OriginRegistry.get(new Identifier(originId));
    }

    /**
     * Call every registered listener for this event
     *
     * @param event The event that is being emitted.
     */
    public static void emit(@NotNull PlayerOriginUpdateEvent event) {
        LISTENERS.forEach(listener -> listener.accept(event));
    }

    /**
     * Add a listener for this event
     *
     * @param listener The listener to subscribe to the event.
     */
    public static void subscribe(@NotNull Consumer<PlayerOriginUpdateEvent> listener) {
        LISTENERS.add(listener);
    }

    /**
     * Gives you the player of the event
     *
     * @return The player that changed its origin.
     */
    public @NotNull ServerPlayerEntity getPlayer() {
        return player;
    }

    /**
     * Gives you the origin of the event
     *
     * @return The origin of the player.
     */
    public @NotNull Origin getOrigin() {
        return origin;
    }
}

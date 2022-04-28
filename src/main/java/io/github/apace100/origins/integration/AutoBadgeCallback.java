package io.github.apace100.origins.integration;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.screen.badge.BadgeFactory;
import io.github.apace100.origins.screen.badge.BadgeManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

/**
 * Callback which is called when a power hasn't got any badges from json and was expecting a fallback<br>
 * Badge fallbacks can be added on this callback.<br>
 * The provided power id always refers to the power where the badge is attaching to.<br>
 * Use {@link BadgeFactory#read(PowerType, JsonObject)} to create a {@link BadgeFactory.Instance}.<br>
 * Use {@link BadgeManager#addBadge(Identifier, BadgeFactory.Instance)} to add a badge to the {@link BadgeManager}<br>
 */
public interface AutoBadgeCallback {

    Event<AutoBadgeCallback> EVENT = EventFactory.createArrayBacked(AutoBadgeCallback.class,
        (listeners) -> (manager, powerType, powerId, isSubPower) -> {
            for(AutoBadgeCallback listener : listeners) {
                listener.registerAutoBadge(manager, powerType, powerId, isSubPower);
            }
        }
    );

    void registerAutoBadge(BadgeManager manager, PowerType<?> powerType, Identifier powerId, boolean isSubPower);

}

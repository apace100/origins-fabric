package io.github.apace100.origins.integration;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.integration.PostPowerReloadCallback;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

/**
 * Callback which is called when a power hasn't got any badges from json and was expecting a fallback<br>
 * Badge fallbacks can be added on this callback.<br>
 * The callback is not informing whether the power is a subpower,<br>
 * as all badges from subpowers will be merged to the main power on {@link PostPowerReloadCallback}.<br>
 * Use {@link BadgeManager#putPowerBadge(Identifier, Badge)} to put a badge to the {@link BadgeManager}<br>
 */
public interface AutoBadgeCallback {

    Event<AutoBadgeCallback> EVENT = EventFactory.createArrayBacked(AutoBadgeCallback.class,
        (listeners) -> (powerId, powerType) -> {
            for(AutoBadgeCallback listener : listeners) {
                listener.createAutoBadge(powerId, powerType);
            }
        }
    );

    void createAutoBadge(Identifier powerId, PowerType<?> powerType);

}

package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.PlayerOriginComponent;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ModComponents {

    public static final ComponentType<OriginComponent> ORIGIN;

    static {
        ORIGIN = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(Origins.MODID, "origin"), OriginComponent.class);
    }

    public static void register() {
        EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(ORIGIN, new PlayerOriginComponent(player)));
        EntityComponents.setRespawnCopyStrategy(ORIGIN, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

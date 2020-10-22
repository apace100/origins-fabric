package io.github.apace100.origins.registry;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.PlayerOriginComponent;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<OriginComponent> ORIGIN;

    static {
        ORIGIN = ComponentRegistry.getOrCreate(new Identifier(Origins.MODID, "origin"), OriginComponent.class);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(ORIGIN, PlayerOriginComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    public static final EntityType ENDERIAN_PEARL;

    static {
        ENDERIAN_PEARL = FabricEntityTypeBuilder.<EnderianPearlEntity>create(SpawnGroup.MISC, (type, world) -> new EnderianPearlEntity(type, world)).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackable(64, 10).build();
    }

    public static void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(Origins.MODID, "enderian_pearl"), ENDERIAN_PEARL);
    }
}

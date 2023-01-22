package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModEntities {

    public static final EntityType<EnderianPearlEntity> ENDERIAN_PEARL;

    static {
        ENDERIAN_PEARL = FabricEntityTypeBuilder.<EnderianPearlEntity>create(SpawnGroup.MISC, EnderianPearlEntity::new).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackable(64, 10).build();
    }

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier(Origins.MODID, "enderian_pearl"), ENDERIAN_PEARL);
    }
}

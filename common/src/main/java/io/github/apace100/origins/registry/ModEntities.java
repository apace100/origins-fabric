package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<EnderianPearlEntity> ENDERIAN_PEARL;

    static {
        ENDERIAN_PEARL = EntityType.Builder.<EnderianPearlEntity>create(EnderianPearlEntity::new, SpawnGroup.MISC).setDimensions(0.25F, 0.25F).maxTrackingRange(64).trackingTickInterval(10).build("enderian_pearl");
    }

    public static void register() {
        ModRegistriesArchitectury.ENTITY_TYPES.register(new Identifier(Origins.MODID, "enderian_pearl"), () -> ENDERIAN_PEARL);
    }
}

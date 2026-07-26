package io.github.apace100.origins.power.type;

import com.google.common.collect.Iterables;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LikeWaterPowerType extends PowerType {

    public LikeWaterPowerType(Optional<EntityCondition> condition) {
        super(condition);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return OriginsPowerTypes.LIKE_WATER;
    }

    public static Vec3d modify(Entity entity, Vec3d motion) {
        return doesApply(entity, motion)
            ? new Vec3d(motion.getX(), 0, motion.getZ())
            : motion;
    }

    private static boolean doesApply(Entity entity, Vec3d motion) {
        return Math.abs(motion.getY()) < 0.025
            && Iterables.isEmpty(entity.getWorld().getBlockCollisions(entity, entity.getBoundingBox().stretch(0.0, motion.getY(), 0.0)))
            && PowerHolderComponent.hasPowerType(entity, LikeWaterPowerType.class);
    }

}

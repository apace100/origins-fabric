package io.github.apace100.origins.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public class LikeWaterPowerType extends PowerType {

    public LikeWaterPowerType(Optional<EntityCondition> condition) {
        super(condition);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return OriginsPowerTypes.LIKE_WATER;
    }

    public static Vec3d modify(Entity entity, double gravity, Vec3d motion, Supplier<Vec3d> defaultGetter) {
        return doesApply(entity, gravity, motion)
            ? new Vec3d(motion.x, 0, motion.z)
            : defaultGetter.get();
    }

    private static boolean doesApply(Entity entity, double gravity, Vec3d motion) {
        double force = Math.abs(motion.y - gravity / 16.0) - 0.005;
        return Math.signum(gravity) >= 1.0
            && Math.signum(force) >= 1.0
            && force < 0.025
            && !entity.isSprinting()
            && PowerHolderComponent.hasPowerType(entity, LikeWaterPowerType.class);
    }

}

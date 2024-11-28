package io.github.apace100.origins.power.type;

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

    public static Vec3d modifyFluidMovement(Entity entity, Vec3d velocity, double fallVelocity) {
        return PowerHolderComponent.hasPowerType(entity, LikeWaterPowerType.class) && Math.abs(velocity.y - fallVelocity / 16.0D) < 0.025D
            ? new Vec3d(velocity.x, 0, velocity.z)
            : velocity;
    }

}

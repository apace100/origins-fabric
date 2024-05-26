package io.github.apace100.origins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class LikeWaterPower extends Power {

    public LikeWaterPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public static Vec3d modifyFluidMovement(Entity entity, Vec3d velocity, double fallVelocity) {
        return PowerHolderComponent.hasPower(entity, LikeWaterPower.class) && Math.abs(velocity.y - fallVelocity / 16.0D) < 0.025D
            ? new Vec3d(velocity.x, 0, velocity.z)
            : velocity;
    }

}

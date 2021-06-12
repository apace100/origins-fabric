package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class NoCobwebSlowdownMixin extends LivingEntity implements Nameable, CommandOutput {
    protected NoCobwebSlowdownMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "slowMovement", cancellable = true)
    public void slowMovement(BlockState state, Vec3d multiplier, CallbackInfo info) {
        if (OriginsPowerTypes.NO_COBWEB_SLOWDOWN.isActive(this) || OriginsPowerTypes.MASTER_OF_WEBS_NO_SLOWDOWN.isActive(this)) {
            info.cancel();
        }
    }
}

package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.origins.power.WaterBreathingPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class WaterBreathingMixin {

    @Mixin(LivingEntity.class)
    public static abstract class BreathingImpl extends Entity {

        private BreathingImpl(EntityType<?> type, World world) {
            super(type, world);
        }

        @ModifyReturnValue(method = "canBreatheInWater", at = @At("RETURN"))
        private boolean origins$breatheUnderwater(boolean original) {
            return original
                || PowerHolderComponent.hasPower(this, WaterBreathingPower.class);
        }

        @Inject(method = "baseTick", at = @At("TAIL"))
        private void origins$waterBreathingTick(CallbackInfo ci) {
            WaterBreathingPower.tick((LivingEntity) (Object) this);
        }

    }

    @Mixin(PlayerEntity.class)
    public static abstract class TurtleHelmetProxy extends LivingEntity {

        private TurtleHelmetProxy(EntityType<? extends LivingEntity> entityType, World world) {
            super(entityType, world);
        }

        @ModifyExpressionValue(method = "updateTurtleHelmet", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
        private boolean origins$submergedProxy(boolean original) {
            return PowerHolderComponent.hasPower(this, WaterBreathingPower.class) != original;
        }

    }

}

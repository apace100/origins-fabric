package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import io.github.apace100.origins.access.WaterMovingEntity;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.IgnoreWaterPower;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.power.SwimmingPower;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WaterMovingEntity {

    private boolean isMoving = false;

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "getUnderwaterVisibility", cancellable = true)
    private void getUnderwaterVisibility(CallbackInfoReturnable<Float> info) {
        if(PowerTypes.WATER_VISION.isActive(this)) {
            info.setReturnValue(1.0F);
        }
    }

    @Inject(at = @At("HEAD"), method = "isSubmergedInWater", cancellable = true)
    private void allowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        if(OriginComponent.hasPower(this, SwimmingPower.class)) {
            cir.setReturnValue(true);
        } else if(OriginComponent.hasPower(this, IgnoreWaterPower.class)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "tickMovement")
    private void beginMovementPhase(CallbackInfo ci) {
        isMoving = true;
    }

    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void endMovementPhase(CallbackInfo ci) {
        isMoving = false;
    }

    public boolean isInMovementPhase() {
        return isMoving;
    }
}

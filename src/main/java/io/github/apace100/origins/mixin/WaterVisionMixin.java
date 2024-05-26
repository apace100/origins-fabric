package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.origins.power.WaterVisionPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class WaterVisionMixin extends AbstractClientPlayerEntity {

    private WaterVisionMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyExpressionValue(method = "getUnderwaterVisibility", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;underwaterVisibilityTicks:I", ordinal = 0))
    private int origins$ignoreVisibilityDelay(int original) {
        return !PowerHolderComponent.hasPower(this, WaterVisionPower.class)
            ? original
            : 1000;
    }

}

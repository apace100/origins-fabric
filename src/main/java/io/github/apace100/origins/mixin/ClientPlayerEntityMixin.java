package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ElytraFlightPower;
import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    // ELYTRA
    @Inject(method = "tickMovement", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private void allowElytrianFlight(CallbackInfo info) {
        if(!this.isFallFlying() && OriginComponent.getPowers(this, ElytraFlightPower.class).size() > 0) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @Inject(at = @At("HEAD"), method = "getUnderwaterVisibility", cancellable = true)
    private void getUnderwaterVisibility(CallbackInfoReturnable<Float> info) {
        if(PowerTypes.WATER_VISION.isActive(this)) {
            info.setReturnValue(1.0F);
        }
    }
}

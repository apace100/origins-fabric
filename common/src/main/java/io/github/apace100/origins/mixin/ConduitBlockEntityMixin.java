package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin {

    @Redirect(method = "givePlayersEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"))
    private boolean allowConduitPowerOnLand(PlayerEntity playerEntity) {
        return playerEntity.isTouchingWaterOrRain() || PowerTypes.CONDUIT_POWER_ON_LAND.isActive(playerEntity);
    }
}

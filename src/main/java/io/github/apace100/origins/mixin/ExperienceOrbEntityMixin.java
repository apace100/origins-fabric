package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyExperiencePower;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {

    @Shadow private int amount;

    @Inject(method = "onPlayerCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;experiencePickUpDelay:I", ordinal = 1))
    private void modifyXpAmount(PlayerEntity player, CallbackInfo ci) {
        this.amount = (int)OriginComponent.modify(player, ModifyExperiencePower.class, this.amount);
    }
}

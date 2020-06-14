package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.tag.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"), method = "render")
    private static boolean hasStatusEffectProxy(LivingEntity player, StatusEffect effect) {
        if(effect == StatusEffects.NIGHT_VISION && player.isSubmergedIn(FluidTags.WATER) && PowerTypes.WATER_VISION.isActive(player)) {
            return true;
        } else {
            return player.hasStatusEffect(effect);
        }
    }
}

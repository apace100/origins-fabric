package io.github.apace100.origins.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.LavaVisionPower;
import io.github.apace100.origins.power.NightVisionPower;
import io.github.apace100.origins.power.PhasingPower;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.ClientHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;

@Mixin(BackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 1), method = "render")
    private static boolean hasStatusEffectProxy(LivingEntity player, StatusEffect effect) {
        if(player instanceof PlayerEntity && effect == StatusEffects.NIGHT_VISION && !player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return ModComponents.getOriginComponent(player).getPowers(NightVisionPower.class).stream().anyMatch(NightVisionPower::isActive);
        }
        return player.hasStatusEffect(effect);
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 0), ordinal = 0)
    private static double modifyD(double original, Camera camera) {
        if(camera.getFocusedEntity() instanceof PlayerEntity) {
            if(OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(ClientHooks.getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    return 0;
                }
            }
        }
        return original;
    }

    /*@ModifyVariable(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"), ordinal = 0)
    private static float modifyS(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getS();
        }
        return original;
    }

    @ModifyVariable(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"), ordinal = 1)
    private static float modifyV(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getV();
        }
        return original;
    }
    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 0))
    private static boolean allowUnderlavaVision(LivingEntity livingEntity, StatusEffect effect) {
        //if(PowerTypes.LAVA_SWIMMING.isActive(livingEntity)) {
        //    return true;
        //}
        return livingEntity.hasStatusEffect(effect);
    }

    @ModifyConstant(method = "applyFog", constant = @Constant(floatValue = 3.0F, ordinal = 0))
    private static float modifyLavaVisibility(float original, Camera camera) {
        //if(PowerTypes.LAVA_SWIMMING.isActive(camera.getFocusedEntity())) {
        //    return original * 5F;
        //}
        return original;
    }

 */
}

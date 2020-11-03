package io.github.apace100.origins.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.LavaVisionPower;
import io.github.apace100.origins.power.NightVisionPower;
import io.github.apace100.origins.power.PhasingPower;
import io.github.apace100.origins.registry.ModComponents;
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
            return ModComponents.ORIGIN.get(player).getPowers(NightVisionPower.class).stream().anyMatch(NightVisionPower::isActive);
        }
        return player.hasStatusEffect(effect);
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 0), ordinal = 0)
    private static double modifyD(double original, Camera camera) {
        if(camera.getFocusedEntity() instanceof PlayerEntity) {
            if(OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
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
    }*/

    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"))
    private static void redirectFogStart(float start, Camera camera, BackgroundRenderer.FogType fogType) {
        if(camera.getFocusedEntity() instanceof PlayerEntity) {
            List<PhasingPower> phasings = OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    float s;
                    if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        s = Math.min(0F, start);
                    } else {
                        s = Math.min(view * 0.25F, start);
                    }
                    RenderSystem.fogStart(s);
                    return;
                }
            }
        }
        RenderSystem.fogStart(start);
    }

    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogEnd(F)V"))
    private static void redirectFogEnd(float end, Camera camera, BackgroundRenderer.FogType fogType) {
        if(camera.getFocusedEntity() instanceof PlayerEntity) {
            List<PhasingPower> phasings = OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    float v;
                    if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        v = Math.min(view * 0.8F, end);
                    } else {
                        v = Math.min(view, end);
                    }
                    RenderSystem.fogEnd(v);
                    return;
                }
            }
        }
        RenderSystem.fogEnd(end);
    }

    private static BlockState getInWallBlockState(PlayerEntity playerEntity) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.world.getBlockState(mutable);
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.world, mutable)) {
                return blockState;
            }
        }

        return null;
    }
/*
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

 */ @ModifyConstant(method = "applyFog", constant = @Constant(floatValue = 0.25F, ordinal = 0))
    private static float modifyLavaVisibilitySNoPotion(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getS();
        }
        return original;
    }

    @ModifyConstant(method = "applyFog", constant = @Constant(floatValue = 1.0F, ordinal = 1))
    private static float modifyLavaVisibilityVNoPotion(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getV();
        }
        return original;
    }

    @ModifyConstant(method = "applyFog", constant = @Constant(floatValue = 0.0F, ordinal = 0))
    private static float modifyLavaVisibilitySWithPotion(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getS();
        }
        return original;
    }

    @ModifyConstant(method = "applyFog", constant = @Constant(floatValue = 3.0F, ordinal = 0))
    private static float modifyLavaVisibilityVWithPotion(float original, Camera camera) {
        List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
        if(powers.size() > 0) {
            return powers.get(0).getV();
        }
        return original;
    }
}

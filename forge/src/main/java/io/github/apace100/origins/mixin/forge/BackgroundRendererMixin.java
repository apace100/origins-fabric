package io.github.apace100.origins.mixin.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.LavaVisionPower;
import io.github.apace100.origins.power.PhasingPower;
import io.github.apace100.origins.util.ClientHooks;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"), remap = false)
	private static void redirectFogStart(float start, Camera camera, BackgroundRenderer.FogType fogType) {
		if (camera.getFocusedEntity() instanceof PlayerEntity) {
			List<PhasingPower> phasings = OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
			if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
				if (ClientHooks.getInWallBlockState((PlayerEntity) camera.getFocusedEntity()) != null) {
					float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).orElseThrow(RuntimeException::new);
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

	@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogEnd(F)V"), remap = false)
	private static void redirectFogEnd(float end, Camera camera, BackgroundRenderer.FogType fogType) {
		if (camera.getFocusedEntity() instanceof PlayerEntity) {
			List<PhasingPower> phasings = OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
			if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
				if (ClientHooks.getInWallBlockState((PlayerEntity) camera.getFocusedEntity()) != null) {
					float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).orElseThrow(RuntimeException::new);
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

	@ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 0.25F, ordinal = 0), remap = false)
	private static float modifyLavaVisibilitySNoPotion(float original, Camera camera) {
		List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
		if (powers.size() > 0) {
			return powers.get(0).getS();
		}
		return original;
	}

	@ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 1.0F, ordinal = 1), remap = false)
	private static float modifyLavaVisibilityVNoPotion(float original, Camera camera) {
		List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
		if (powers.size() > 0) {
			return powers.get(0).getV();
		}
		return original;
	}

	@ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 0.0F, ordinal = 0), remap = false)
	private static float modifyLavaVisibilitySWithPotion(float original, Camera camera) {
		List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
		if (powers.size() > 0) {
			return powers.get(0).getS();
		}
		return original;
	}

	@ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 3.0F, ordinal = 0), remap = false)
	private static float modifyLavaVisibilityVWithPotion(float original, Camera camera) {
		List<LavaVisionPower> powers = OriginComponent.getPowers(camera.getFocusedEntity(), LavaVisionPower.class);
		if (powers.size() > 0) {
			return powers.get(0).getV();
		}
		return original;
	}
}

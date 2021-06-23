package io.github.apace100.origins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.origins.OriginsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class PhasingOverlayMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void method_31136(float f);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawPhantomizedOverlay(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if(PowerHolderComponent.getPowers(this.client.player, PhasingPower.class).size() > 0 && !this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            this.method_31136(OriginsClient.config.phantomizedOverlayStrength);
        }
    }
}

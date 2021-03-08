package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.InvisibilityPower;
import io.github.apace100.origins.power.ModelColorPower;
import io.github.apace100.origins.power.ShakingPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity> {

    protected LivingEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void letPlayersShakeTheirBodies(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if(OriginComponent.hasPower(entity, ShakingPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void preventPumpkinRendering(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        List<InvisibilityPower> invisibilityPowers = OriginComponent.getPowers(livingEntity, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderArmor)) {
            info.cancel();
        }
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;", shift = At.Shift.BEFORE))
    private RenderLayer changeRenderLayerWhenTranslucent(RenderLayer original, LivingEntity entity) {
        if(entity instanceof PlayerEntity) {
            if(OriginComponent.getPowers(entity, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
                return RenderLayer.getItemEntityTranslucentCull(getTexture(entity));
            }
        }
        return original;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", ordinal = 0))
    private void renderColorChangedModel(EntityModel model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, LivingEntity player) {
        if(player instanceof PlayerEntity) {
            List<ModelColorPower> modelColorPowers = OriginComponent.getPowers(player, ModelColorPower.class);
            if (modelColorPowers.size() > 0) {
                float r = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
                float g = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
                float b = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, c) -> a * c).get();
                float a = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
                model.render(matrices, vertices, light, overlay, r * red, g * green, b * blue, a * alpha);
                return;
            }
        }
        model.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}

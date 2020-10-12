package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.InvisibilityPower;
import io.github.apace100.origins.power.ModelColorPower;
import io.github.apace100.origins.power.PowerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity> {

    protected LivingEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void preventPumpkinRendering(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if(MinecraftClient.getInstance().player != null && PowerTypes.PUMPKIN_HATE.isActive(MinecraftClient.getInstance().player)) {
            if(livingEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() == Items.CARVED_PUMPKIN) {
                info.cancel();
            }
        }
        List<InvisibilityPower> invisibilityPowers = OriginComponent.getPowers(livingEntity, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderArmor)) {
            info.cancel();
        }
    }
/*
    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;", shift = At.Shift.BEFORE), ordinal = 1)
    private boolean changeRenderLayerWhenTranslucent(boolean original, LivingEntity livingEntity) {
        if(livingEntity instanceof PlayerEntity) {
            if(OriginComponent.getPowers(livingEntity, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
                return true;
            }
        }
        return original;
    }*/

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;", shift = At.Shift.BEFORE))
    private RenderLayer changeRenderLayerWhenTranslucent(RenderLayer original, LivingEntity entity) {
        if(entity instanceof PlayerEntity) {
            if(OriginComponent.getPowers(entity, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
                return RenderLayer.getEntityTranslucent(getTexture(entity));
            }
        }
        return original;
    }
/*
    @ModifyConstant(method = "render", constant = @Constant(floatValue = 0.15F, ordinal = 0))
    private float modifyTranslucency(float original, LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity) {
            List<ModelColorPower> modelColorPowers = ModComponents.ORIGIN.get(livingEntity).getPowers(ModelColorPower.class);
            if (modelColorPowers.size() > 0) {
                return modelColorPowers.stream().map(p -> p.getAlpha()).min(Float::compare).get();
            }
        }
        return original;
    }*/

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

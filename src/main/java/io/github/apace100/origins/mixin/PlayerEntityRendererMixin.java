package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.ModelColorPower;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Environment(EnvType.CLIENT)
    @Redirect(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 0))
    private void makeArmTranslucent(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, MatrixStack matrices2, VertexConsumerProvider vertexConsumers, int light2, AbstractClientPlayerEntity player) {
        List<ModelColorPower> modelColorPowers = ModComponents.ORIGIN.get(player).getPowers(ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).get();
            float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
            modelPart.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, overlay, red, green, blue, alpha);
            return;
        }
        modelPart.render(matrices, vertices, light, overlay);
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 1))
    private void makeSleeveTranslucent(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, MatrixStack matrices2, VertexConsumerProvider vertexConsumers, int light2, AbstractClientPlayerEntity player) {
        List<ModelColorPower> modelColorPowers = ModComponents.ORIGIN.get(player).getPowers(ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).get();
            float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
            modelPart.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, overlay, red, green, blue, alpha);
            return;
        }
        modelPart.render(matrices, vertices, light, overlay);
    }
}

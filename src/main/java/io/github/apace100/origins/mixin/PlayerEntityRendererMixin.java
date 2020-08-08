package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.ModelTranslucencyPower;
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
        List<ModelTranslucencyPower> modelTranslucencyPowers = ModComponents.ORIGIN.get(player).getPowers(ModelTranslucencyPower.class);
        if (modelTranslucencyPowers.size() > 0) {
            float alpha = modelTranslucencyPowers.stream().map(p -> p.value).min(Float::compare).get();
            modelPart.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, overlay, 1F, 1F, 1F, alpha);
            return;
        }
        modelPart.render(matrices, vertices, light, overlay);
    }
}

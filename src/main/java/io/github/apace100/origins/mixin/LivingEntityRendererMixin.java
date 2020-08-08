package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.ModelTranslucencyPower;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
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
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"), name = "bl2")
    private boolean modifyTranslucency(boolean original, LivingEntity livingEntity) {
        if(livingEntity instanceof PlayerEntity) {
            if(ModComponents.ORIGIN.get(livingEntity).getPowers(ModelTranslucencyPower.class).size() > 0) {
                return true;
            }
        }
        return original;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 0.15F, ordinal = 0))
    private float modifyTranslucency(float original, LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity) {
            List<ModelTranslucencyPower> modelTranslucencyPowers = ModComponents.ORIGIN.get(livingEntity).getPowers(ModelTranslucencyPower.class);
            if (modelTranslucencyPowers.size() > 0) {
                return modelTranslucencyPowers.stream().map(p -> p.value).min(Float::compare).get();
            }
        }
        return original;
    }
}

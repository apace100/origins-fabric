package io.github.apace100.origins.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.NightVisionPower;
import io.github.apace100.origins.power.PhasingPower;
import io.github.apace100.origins.power.ShaderPower;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ItemStack floatingItem;

    @Shadow protected abstract void method_31136(float f);

    @Shadow protected abstract void loadShader(Identifier identifier);

    @Shadow private ShaderEffect shader;
    @Shadow private boolean shadersEnabled;
    @Unique
    private Identifier currentlyLoadedShader;

    @Inject(at = @At("TAIL"), method = "onCameraEntitySet")
    private void loadShaderFromPowerOnCameraEntity(Entity entity, CallbackInfo ci) {
        OriginComponent.withPower(client.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            loadShader(shaderLoc);
            currentlyLoadedShader = shaderLoc;
        });
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void loadShaderFromPower(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        OriginComponent.withPower(client.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(currentlyLoadedShader != shaderLoc) {
                loadShader(shaderLoc);
                currentlyLoadedShader = shaderLoc;
            }
        });
        if(!OriginComponent.hasPower(client.getCameraEntity(), ShaderPower.class) && currentlyLoadedShader != null) {
            this.shader.close();
            this.shader = null;
            this.shadersEnabled = false;
            currentlyLoadedShader = null;
        }
    }

    @Inject(
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"),
        method = "render"
    )
    private void fixHudWithShaderEnabled(float tickDelta, long nanoTime, boolean renderLevel, CallbackInfo info) {
        RenderSystem.enableTexture();
    }
/*
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setCameraEntity(Lnet/minecraft/entity/Entity;)V"))
    private void updateShaderPowers(CallbackInfo ci) {
        if(OriginComponent.hasPower(client.getCameraEntity(), ShaderPower.class)) {
            OriginComponent.withPower(client.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
                Identifier shaderLoc = shaderPower.getShaderLocation();
                loadShader(shaderLoc);
                currentlyLoadedShader = shaderLoc;
            });
        } else {
            this.shader.close();
            this.shader = null;
            this.shadersEnabled = false;
            currentlyLoadedShader = null;
        }
    }*/

    // NightVisionPower
    @Inject(at = @At("HEAD"), method = "getNightVisionStrength", cancellable = true)
    private static void getNightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> info) {
        if (livingEntity instanceof PlayerEntity && !livingEntity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            List<NightVisionPower> nvs = ModComponents.ORIGIN.get(livingEntity).getPowers(NightVisionPower.class);
            Optional<Float> strength = nvs.stream().filter(NightVisionPower::isActive).map(NightVisionPower::getStrength).max(Float::compareTo);
            strength.ifPresent(info::setReturnValue);
        }
    }

    private HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    // PHASING: remove_blocks
    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        List<PhasingPower> phasings = OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
        if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
            Set<BlockPos> eyePositions = getEyePos(0.25F, 0.05F, 0.25F);
            Set<BlockPos> noLongerEyePositions = new HashSet<>();
            for (BlockPos p : savedStates.keySet()) {
                if (!eyePositions.contains(p)) {
                    noLongerEyePositions.add(p);
                }
            }
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                client.world.setBlockState(eyePosition, state);
                savedStates.remove(eyePosition);
            }
            for (BlockPos p : eyePositions) {
                BlockState stateAtP = client.world.getBlockState(p);
                if (!savedStates.containsKey(p) && !client.world.isAir(p) && !(stateAtP.getBlock() instanceof FluidBlock)) {
                    savedStates.put(p, stateAtP);
                    client.world.setBlockStateWithoutNeighborUpdates(p, Blocks.AIR.getDefaultState());
                }
            }
        } else if (savedStates.size() > 0) {
            Set<BlockPos> noLongerEyePositions = new HashSet<>(savedStates.keySet());
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                client.world.setBlockState(eyePosition, state);
                savedStates.remove(eyePosition);
            }
        }
    }

    // PHASING
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"), method = "renderWorld")
    private void preventThirdPerson(Camera camera, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        if (OriginComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            camera.update(area, focusedEntity, false, false, tickDelta);
        } else {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3d pos = camera.getFocusedEntity().getPos().add(0, camera.getFocusedEntity().getEyeHeight(camera.getFocusedEntity().getPose()), 0);
        Box cameraBox = new Box(pos, pos);
        cameraBox = cameraBox.expand(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.stream(cameraBox).forEach(p -> set.add(p.toImmutable()));
        return set;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void drawPhantomizedOverlay(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if(OriginComponent.getPowers(this.client.player, PhasingPower.class).size() > 0 && !this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            this.method_31136(OriginsClient.config.phantomizedOverlayStrength);
        }
    }
}

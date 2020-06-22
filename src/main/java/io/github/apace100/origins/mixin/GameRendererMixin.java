package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ItemStack floatingItem;

    @Inject(at = @At("HEAD"), method = "getNightVisionStrength", cancellable = true)
    private static void getNightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> info) {
        if (livingEntity != null && livingEntity.isSubmergedIn(FluidTags.WATER) && PowerTypes.WATER_VISION.isActive(livingEntity)) {
            info.setReturnValue(1F);
        }
    }

    private HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        if (PowerTypes.PHASING.isActive(camera.getFocusedEntity()) && PowerTypes.PHASING.get(camera.getFocusedEntity()).isActive()) {
            Set<BlockPos> eyePositions = getEyePos();
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
                if (!savedStates.containsKey(p) && !client.world.isAir(p)) {
                    savedStates.put(p, client.world.getBlockState(p));
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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"), method = "renderWorld")
    private void preventThirdPerson(Camera camera, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        if (PowerTypes.PHASING.isActive(camera.getFocusedEntity()) && PowerTypes.PHASING.get(camera.getFocusedEntity()).isActive()) {
            camera.update(area, focusedEntity, false, false, tickDelta);
        } else {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    private Set<BlockPos> getEyePos() {
        Vec3d pos = camera.getFocusedEntity().getPos().add(0, camera.getFocusedEntity().getEyeHeight(camera.getFocusedEntity().getPose()), 0);
        Box cameraBox = new Box(pos, pos);
        cameraBox = cameraBox.expand(0.25, 0.05, 0.25);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.method_29715(cameraBox).forEach(p -> set.add(p.toImmutable()));
        return set;
    }
}

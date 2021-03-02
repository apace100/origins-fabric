package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void makeFullyFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if(OriginComponent.hasPower((Entity)(Object)this, FireImmunityPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @Shadow public World world;

    @Shadow public abstract double getFluidHeight(Tag<Fluid> fluid);

    @Inject(at = @At("HEAD"), method = "isInvulnerableTo", cancellable = true)
    private void makeOriginInvulnerable(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof PlayerEntity) {
            OriginComponent component = ModComponents.getOriginComponent((Entity)(Object)this);
            if(!component.hasAllOrigins()) {
                cir.setReturnValue(true);
            }
            if(component.getPowers(InvulnerablePower.class).stream().anyMatch(inv -> inv.doesApply(damageSource))) {
                cir.setReturnValue(true);
            }
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isWet()Z"))
    private boolean preventExtinguishingFromSwimming(Entity entity) {
        if(OriginComponent.hasPower(entity, SwimmingPower.class) && entity.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            return false;
        }
        return entity.isWet();
    }

    @Inject(at = @At("HEAD"), method = "isInvisible", cancellable = true)
    private void phantomInvisibility(CallbackInfoReturnable<Boolean> info) {
        if(OriginComponent.hasPower((Entity)(Object)this, InvisibilityPower.class)) {
            info.setReturnValue(true);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), method = "pushOutOfBlocks", cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo info) {
        List<PhasingPower> powers = OriginComponent.getPowers((Entity)(Object)this, PhasingPower.class);
        if(powers.size() > 0) {
            if(powers.stream().anyMatch(phasingPower -> phasingPower.doesApply(new BlockPos(x, y, z)))) {
                info.cancel();
            }
        }
    }
}

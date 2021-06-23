package io.github.apace100.origins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin delays the deserialization of the origin component,
 * to guarantee that it loads after the power component of Apoli
 * has loaded.
 */
@Mixin(value = PowerHolderComponentImpl.class, remap = false)
public class OriginDeserializationMixin {

    @Shadow @Final private LivingEntity owner;

    @Inject(method = "readFromNbt", at = @At("TAIL"))
    private void loadOriginAfterPowers(NbtCompound compoundTag, CallbackInfo ci) {
        if(this.owner instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(this.owner);
            component.onPowersRead();
        }
    }
}

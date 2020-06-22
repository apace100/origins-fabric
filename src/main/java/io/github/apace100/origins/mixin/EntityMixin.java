package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "isInvisible", cancellable = true)
    private void phantomInvisibility(CallbackInfoReturnable<Boolean> info) {
        if(PowerTypes.INVISIBILITY.isActive((Entity)(Object)this) && PowerTypes.INVISIBILITY.get((Entity)(Object)this).isActive()) {
            info.setReturnValue(true);
        }
    }
}

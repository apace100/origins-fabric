package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EntityShapeContextAccess;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityShapeContext.class)
public class EntityShapeContextMixin implements EntityShapeContextAccess {
    private Entity entity;

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/EntityShapeContext;<init>(Lnet/minecraft/entity/Entity;)V")
    private void setEntityField(Entity entity, CallbackInfo info) {
        this.entity = entity;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }
}

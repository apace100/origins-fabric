package io.github.apace100.origins.mixin;


import io.github.apace100.origins.power.ScareCreepersPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class ScareCreepersMixin extends LivingEntity implements Targeter {

    private ScareCreepersMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;initGoals()V", shift = At.Shift.AFTER))
    private void origins$modifyGoals(EntityType<?> entityType, World world, CallbackInfo ci) {

        if ((MobEntity) (Object) this instanceof CreeperEntity thisAsCreeper) {
            ScareCreepersPower.modifyGoals(thisAsCreeper);
        }

    }

}

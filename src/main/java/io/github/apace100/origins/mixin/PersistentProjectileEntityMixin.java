package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyProjectileDamagePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @ModifyVariable(method = "onEntityHit", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onAttacking(Lnet/minecraft/entity/Entity;)V"))
    private int modifyProjectileDamageDealt(int original, EntityHitResult entityHitResult) {
        Entity owner = ((ProjectileEntity)(Object)this).getOwner();
        if(owner != null) {
            Entity target = entityHitResult.getEntity();
            DamageSource source = DamageSource.arrow((PersistentProjectileEntity)(Object)this, owner);
            return (int) OriginComponent.modify(owner, ModifyProjectileDamagePower.class, original, p -> p.doesApply(source, original, target instanceof LivingEntity ? (LivingEntity)target : null), p -> p.executeActions(target));
        }
        return original;
    }

    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean preventDamageWhenZero(Entity entity, DamageSource source, float amount) {
        if(entity instanceof ServerPlayerEntity || amount > 0f) {
            return entity.damage(source, amount);
        }
        return false;
    }
}

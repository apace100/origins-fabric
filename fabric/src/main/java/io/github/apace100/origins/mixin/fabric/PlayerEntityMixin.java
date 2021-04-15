package io.github.apace100.origins.mixin.fabric;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyDamageDealtPower;
import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	// AQUA_AFFINITY
	@ModifyConstant(method = "getBlockBreakingSpeed", constant = @Constant(ordinal = 0, floatValue = 5.0F))
	private float modifyWaterBlockBreakingSpeed(float in) {
		if(PowerTypes.AQUA_AFFINITY.isActive(this)) {
			return 1F;
		}
		return in;
	}

	// AQUA_AFFINITY
	@ModifyConstant(method = "getBlockBreakingSpeed", constant = @Constant(ordinal = 1, floatValue = 5.0F))
	private float modifyUngroundedBlockBreakingSpeed(float in) {
		if(this.isInsideWaterOrBubbleColumn() && PowerTypes.AQUA_AFFINITY.isActive(this)) {
			return 1F;
		}
		return in;
	}

	// ModifyDamageDealt
	@ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), name = "f", ordinal = 0)
	public float modifyDamage(float f, Entity target) {
		DamageSource source = DamageSource.player((PlayerEntity)(Object)this);
        return OriginComponent.modify(this, ModifyDamageDealtPower.class, f, p -> p.doesApply(source, f, target instanceof LivingEntity ? (LivingEntity)target : null), p -> p.executeActions(target));
	}
}

package io.github.apace100.origins.mixin.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ElytraFlightPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> entityTypeIn, World worldIn) { super(entityTypeIn, worldIn); }

	@ModifyArg(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V"
	), method = "initAi", index = 1)
	public boolean updateFallFlying(boolean value) {
		boolean bl = this.getFlag(7);

		if (bl && this.isOnGround() && this.hasVehicle()) {
			return value || OriginComponent.getPowers(this, ElytraFlightPower.class).size() > 0;
		}
		return value;
	}
}

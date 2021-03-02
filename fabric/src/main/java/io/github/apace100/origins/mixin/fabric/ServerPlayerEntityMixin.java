package io.github.apace100.origins.mixin.fabric;

import com.mojang.authlib.GameProfile;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyDamageTakenPower;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
	public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(world, blockPos, f, gameProfile);
	}

	@ModifyArg(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private float modifyDamageAmount(DamageSource source, float originalAmount) {
		return OriginComponent.modify(this, ModifyDamageTakenPower.class, originalAmount, p -> p.doesApply(source, originalAmount));
	}
}

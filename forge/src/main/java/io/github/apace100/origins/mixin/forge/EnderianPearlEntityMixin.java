package io.github.apace100.origins.mixin.forge;

import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderianPearlEntity.class)
public abstract class EnderianPearlEntityMixin extends ThrownItemEntity {
	public EnderianPearlEntityMixin(EntityType<? extends ThrownItemEntity> arg, World arg2) { super(arg, arg2); }

	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}

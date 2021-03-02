package io.github.apace100.origins.components;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.PlayerOriginComponent;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgePlayerOriginComponent extends PlayerOriginComponent implements ICapabilityProvider, ICapabilitySerializable<Tag> {

	public ForgePlayerOriginComponent(PlayerEntity player) {
		super(player);
	}

	private final transient LazyOptional<OriginComponent> thisOptional = LazyOptional.of(() -> this);

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
		return ModComponentsImpl.ORIGIN_COMPONENT_CAPABILITY.orEmpty(capability, this.thisOptional);
	}

	@Override
	public Tag serializeNBT() {
		return ModComponentsImpl.ORIGIN_COMPONENT_CAPABILITY.writeNBT(this, null);
	}

	@Override
	public void deserializeNBT(Tag arg) {
		ModComponentsImpl.ORIGIN_COMPONENT_CAPABILITY.readNBT(this, null, arg);
	}
}

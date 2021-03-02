package io.github.apace100.origins.mixin.fabric;

import io.github.apace100.origins.origin.OriginManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OriginManager.class)
public abstract class OriginManagerMixin implements IdentifiableResourceReloadListener {

	@Shadow(remap = false)
	@Override
	public abstract Identifier getFabricId();
}

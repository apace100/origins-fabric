package io.github.apace100.origins.mixin.fabric;

import io.github.apace100.origins.origin.OriginLayers;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OriginLayers.class)
public abstract class OriginLayersMixin implements IdentifiableResourceReloadListener {

	@Shadow(remap = false)
	@Override
	public abstract Identifier getFabricId();
}

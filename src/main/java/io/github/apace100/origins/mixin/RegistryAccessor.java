package io.github.apace100.origins.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Registry.class)
public interface RegistryAccessor {

	@Invoker
	static <T> RegistryKey<Registry<T>> callCreateRegistryKey(String registryId) {
		throw new RuntimeException("Invoker body compilation");
	}

	@Invoker
	static <T> Registry<T> callCreate(RegistryKey<Registry<T>> registryKey, Supplier<T> defaultEntry) {
		throw new RuntimeException("Invoker body compilation");
	}
}

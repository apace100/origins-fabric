package io.github.apace100.origins.util;

import com.mojang.datafixers.types.Func;
import com.mojang.serialization.Lifecycle;
import me.shedaniel.architectury.registry.Registries;
import me.shedaniel.architectury.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class VanillaWrappedRegistry<S> extends MutableRegistry<S> {

	private final Function<Registries, Registry<S>> toArchRegistry;
	private final Lifecycle lifecycle;
	private final Registry<S> archRegistry;
	private final Map<S, Lifecycle> entryToLifecycle;
	private final Map<String, Registry<S>> archRegistries;

	public static <T> VanillaWrappedRegistry<T> wrap(Registry<T> arch) {
		return wrap(arch, Lifecycle.stable());
	}

	@SuppressWarnings({"unchecked"})
	public static <T> VanillaWrappedRegistry<T> wrap(Registry<T> arch, Lifecycle lifecycle) {
		Function<Registries, Registry<T>> toArch = s -> s.get((RegistryKey<net.minecraft.util.registry.Registry<T>>) arch.key());
		return new VanillaWrappedRegistry<>(arch, toArch, lifecycle);
	}

	public static <T, S> VanillaWrappedRegistry<T> wrap(Registry<T> arch, Function<T, S> to, Function<S, T> from) {
		return wrap(arch, to, from, Lifecycle.stable());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T, S> VanillaWrappedRegistry<T> wrap(Registry<T> arch, Function<T, S> to, Function<S, T> from, Lifecycle lifecycle) {
		RegistryKey<net.minecraft.util.registry.Registry<S>> key = (RegistryKey) arch.key();
		Function<Registries, Registry<T>> toArch = s -> new ArchitecturyWrappedRegistry<>(s.get(key), to, from);
		return new VanillaWrappedRegistry<>(arch, toArch, lifecycle);
	}

	public VanillaWrappedRegistry(Registry<S> archRegistry, Function<Registries, Registry<S>> toArchRegistry, Lifecycle lifecycle) {
		super(archRegistry.key(), lifecycle);
		this.archRegistry = archRegistry;
		this.toArchRegistry = toArchRegistry;
		this.lifecycle = lifecycle;
		this.entryToLifecycle = new HashMap<>();
		this.archRegistries = new HashMap<>();
	}

	@Nullable
	@Override
	public Identifier getId(S object) {
		return this.archRegistry.getId(object);
	}

	@Override
	public Optional<RegistryKey<S>> getKey(S object) {
		return this.archRegistry.getKey(object);
	}

	@Override
	public int getRawId(@Nullable S object) {
		return this.archRegistry.getRawId(object);
	}

	@Nullable
	@Override
	public S get(int i) {
		return this.archRegistry.byRawId(i);
	}

	@Nullable
	@Override
	public S get(@Nullable RegistryKey<S> registryKey) {
		return this.get(registryKey == null ? null : registryKey.getValue());
	}

	@Nullable
	@Override
	public S get(@Nullable Identifier identifier) {
		return this.archRegistry.get(identifier);
	}

	@Override
	protected Lifecycle getEntryLifecycle(S object) {
		return this.entryToLifecycle.getOrDefault(object, Lifecycle.stable());
	}

	@Override
	public Lifecycle getLifecycle() {
		return this.lifecycle;
	}

	@Override
	public Set<Identifier> getIds() {
		return this.archRegistry.getIds();
	}

	@Override
	public Set<Map.Entry<RegistryKey<S>, S>> getEntries() {
		return this.archRegistry.entrySet();
	}

	@Override
	public boolean containsId(Identifier identifier) {
		return this.archRegistry.contains(identifier);
	}

	@NotNull
	@Override
	public Iterator<S> iterator() {
		return this.archRegistry.iterator();
	}

	@Override
	public <V extends S> V set(int i, RegistryKey<S> registryKey, V object, Lifecycle lifecycle) {
		throw new UnsupportedOperationException("Set is unsupported on wrapped registries.");
	}

	@Override
	public <V extends S> V add(RegistryKey<S> registryKey, V object, Lifecycle lifecycle) {
		this.archRegistries.computeIfAbsent(registryKey.getValue().getNamespace(), x -> this.toArchRegistry.apply(Registries.get(x)))
				.registerSupplied(registryKey.getValue(), () -> object);
		this.entryToLifecycle.put(object, lifecycle);
		return object;
	}

	@Override
	public <V extends S> V replace(OptionalInt optionalInt, RegistryKey<S> registryKey, V object, Lifecycle lifecycle) {
		throw new UnsupportedOperationException("Replace is unsupported on wrapped registries.");
	}
}

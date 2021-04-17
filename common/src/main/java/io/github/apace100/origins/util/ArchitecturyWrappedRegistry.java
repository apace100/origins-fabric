package io.github.apace100.origins.util;

import me.shedaniel.architectury.core.RegistryEntry;
import me.shedaniel.architectury.registry.Registry;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a converting registry for the purposes of bypassing forge's unique class restriction.
 *
 * @param <T> The type of the wrapped registry.
 * @param <S> The underlying type of the source registry.
 */
public class ArchitecturyWrappedRegistry<T, S> implements Registry<T> {

	private final Registry<S> sourceRegistry;
	private final Function<T, S> to;
	private final Function<S, T> from;

	public ArchitecturyWrappedRegistry(Registry<S> sourceRegistry, Function<T, S> to, Function<S, T> from) {
		this.sourceRegistry = sourceRegistry;
		this.to = to;
		this.from = from;
	}

	@Override
	public @NotNull RegistrySupplier<T> delegateSupplied(Identifier id) {
		return new WrappedRegistrySupplier(sourceRegistry.delegateSupplied(id));
	}

	@Override
	public @NotNull RegistrySupplier<T> registerSupplied(Identifier id, Supplier<T> supplier) {
		return new WrappedRegistrySupplier(sourceRegistry.registerSupplied(id, () -> this.to.apply(supplier.get())));
	}

	@Override
	public @Nullable Identifier getId(T obj) {
		return sourceRegistry.getId(this.to.apply(obj));
	}

	@Override
	public int getRawId(T obj) {
		return sourceRegistry.getRawId(this.to.apply(obj));
	}

	@Override
	public Optional<RegistryKey<T>> getKey(T obj) {
		return sourceRegistry.getKey(this.to.apply(obj)).map(this::convert);
	}

	@Override
	public @Nullable T get(Identifier id) {
		return from.apply(sourceRegistry.get(id));
	}

	@Override
	public @Nullable T byRawId(int rawId) {
		return from.apply(sourceRegistry.byRawId(rawId));
	}

	@Override
	public boolean contains(Identifier id) {
		return sourceRegistry.contains(id);
	}

	@Override
	public boolean containsValue(T obj) {
		return sourceRegistry.containsValue(to.apply(obj));
	}

	@Override
	public Set<Identifier> getIds() {
		return sourceRegistry.getIds();
	}

	@Override
	public Set<Map.Entry<RegistryKey<T>, T>> entrySet() {
		return sourceRegistry.entrySet().stream().map(x -> new Map.Entry<RegistryKey<T>, T>() {
			@Override
			public RegistryKey<T> getKey() {
				return convert(x.getKey());
			}

			@Override
			public T getValue() {
				return from.apply(x.getValue());
			}

			@Override
			public T setValue(T t) {
				return from.apply(x.setValue(to.apply(t)));
			}
		}).collect(Collectors.toSet());
	}

	@Override
	@SuppressWarnings("unchecked")
	public RegistryKey<? extends net.minecraft.util.registry.Registry<T>> key() {
		return (RegistryKey<? extends net.minecraft.util.registry.Registry<T>>) sourceRegistry.key();
	}

	@SuppressWarnings("unchecked")
	private RegistryKey<T> convert(RegistryKey<S> key) {
		//FIXME this implementation is meh, because technically keys ARE the same, but it might no be the best way to do this.
		return (RegistryKey<T>) key;
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			final Iterator<S> prev = sourceRegistry.iterator();

			@Override
			public boolean hasNext() {
				return prev.hasNext();
			}

			@Override
			public T next() {
				return from.apply(prev.next());
			}

			@Override
			public void remove() {
				prev.remove();
			}

			@Override
			public void forEachRemaining(Consumer<? super T> action) {
				prev.forEachRemaining(x -> action.accept(from.apply(x)));
			}
		};
	}

	public static class Wrapper<T, W extends Wrapper<T, W>> extends RegistryEntry<W> {
		private final T value;

		public Wrapper(T value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Wrapper<?, ?> wrapper = (Wrapper<?, ?>) o;
			return Objects.equals(value, wrapper.value);
		}

		public T get() {
			return value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "W:" + this.getClass().getSimpleName() + ":{" + value.toString() + "}";
		}
	}

	public class WrappedRegistrySupplier implements RegistrySupplier<T> {

		private final RegistrySupplier<S> source;

		public WrappedRegistrySupplier(RegistrySupplier<S> source) {
			this.source = source;
		}

		@Override
		public @NotNull Identifier getRegistryId() {
			return this.source.getRegistryId();
		}

		@Override
		public @NotNull Identifier getId() {
			return this.source.getId();
		}

		@Override
		public boolean isPresent() {
			return this.source.isPresent();
		}

		@Override
		public T get() {
			return from.apply(this.source.get());
		}
	}
}

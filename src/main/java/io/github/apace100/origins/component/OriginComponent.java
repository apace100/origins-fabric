package io.github.apace100.origins.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.ValueModifyingPower;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.AttributeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface OriginComponent extends AutoSyncedComponent, ServerTickingComponent {

	boolean hasOrigin(OriginLayer layer);
	boolean hasAllOrigins();

	HashMap<OriginLayer, Origin> getOrigins();
	Origin getOrigin(OriginLayer layer);

	boolean hadOriginBefore();

	boolean hasPower(PowerType<?> powerType);
	<T extends Power> T getPower(PowerType<T> powerType);
	List<Power> getPowers();
	<T extends Power> List<T> getPowers(Class<T> powerClass);
	<T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive);

	void setOrigin(OriginLayer layer, Origin origin);

	void sync();

	static void sync(PlayerEntity player) {
		ModComponents.ORIGIN.sync(player);
	}

	static <T extends Power> void withPower(Entity entity, Class<T> powerClass, Predicate<T> power, Consumer<T> with) {
		if(entity instanceof PlayerEntity) {
			Optional<T> optional = ModComponents.ORIGIN.get(entity).getPowers(powerClass).stream().filter(p -> power == null || power.test(p)).findAny();
			optional.ifPresent(with);
		}
	}

	static <T extends Power> List<T> getPowers(Entity entity, Class<T> powerClass) {
		if(entity instanceof PlayerEntity) {
			return ModComponents.ORIGIN.get(entity).getPowers(powerClass);
		}
		return Lists.newArrayList();
	}

	static <T extends Power> boolean hasPower(Entity entity, Class<T> powerClass) {
		if(entity instanceof PlayerEntity) {
			return ModComponents.ORIGIN.get(entity).getPowers().stream().anyMatch(p -> powerClass.isAssignableFrom(p.getClass()) && p.isActive());
		}
		return false;
	}

	static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue) {
		return (float)modify(entity, powerClass, (double)baseValue, null, null);
	}

	static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter) {
		return (float)modify(entity, powerClass, (double)baseValue, powerFilter, null);
	}

	static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
		return (float)modify(entity, powerClass, (double)baseValue, powerFilter, powerAction);
	}

	static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue) {
		return modify(entity, powerClass, baseValue, null, null);
	}

	static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
		if(entity instanceof PlayerEntity) {
			List<T> powers = ModComponents.ORIGIN.get(entity).getPowers(powerClass);
			List<EntityAttributeModifier> mps = powers.stream()
				.filter(p -> powerFilter == null || powerFilter.test(p))
				.flatMap(p -> p.getModifiers().stream()).collect(Collectors.toList());
			if(powerAction != null) {
				powers.stream().filter(p -> powerFilter == null || powerFilter.test(p)).forEach(powerAction);
			}
			return AttributeUtil.sortAndApplyModifiers(mps, baseValue);
		}
		return baseValue;
	}

	default boolean checkAutoChoosingLayers(PlayerEntity player, boolean includeDefaults) {
		boolean choseOneAutomatically = false;
		ArrayList<OriginLayer> layers = new ArrayList<>();
		for(OriginLayer layer : OriginLayers.getLayers()) {
			if(layer.isEnabled()) {
				layers.add(layer);
			}
		}
		Collections.sort(layers);
		for(OriginLayer layer : layers) {
			boolean shouldContinue = false;
			if (layer.isEnabled() && !hasOrigin(layer)) {
				if (includeDefaults && layer.hasDefaultOrigin()) {
					setOrigin(layer, OriginRegistry.get(layer.getDefaultOrigin()));
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if (layer.getOriginOptionCount(player) == 1 && layer.shouldAutoChoose()) {
					List<Origin> origins = layer.getOrigins(player).stream().map(OriginRegistry::get).filter(Origin::isChoosable).collect(Collectors.toList());
					if (origins.size() == 0) {
						List<Identifier> randomOrigins = layer.getRandomOrigins(player);
						setOrigin(layer, OriginRegistry.get(randomOrigins.get(player.getRandom().nextInt(randomOrigins.size()))));
					} else {
						setOrigin(layer, origins.get(0));
					}
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if(layer.getOriginOptionCount(player) == 0) {
					shouldContinue = true;
				}
			} else {
				shouldContinue = true;
			}
			if(!shouldContinue) {
				break;
			}
		}
		return choseOneAutomatically;
	}
}

package io.github.apace100.origins.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.ValueModifyingPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface OriginComponent extends AutoSyncedComponent {

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
		return (float)modify(entity, powerClass, (double)baseValue, null);
	}

	static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter) {
		return (float)modify(entity, powerClass, (double)baseValue, powerFilter);
	}

	static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue) {
		return modify(entity, powerClass, baseValue, null);
	}

	static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue, Predicate<T> powerFilter) {
		if(entity instanceof PlayerEntity) {
			double currentValue = baseValue;
			List<EntityAttributeModifier> mps = ModComponents.ORIGIN.get(entity).getPowers(powerClass).stream()
				.filter(p -> powerFilter == null || powerFilter.test(p))
				.flatMap(p -> p.getModifiers().stream()).sorted().collect(Collectors.toList());
			for(EntityAttributeModifier modifier : mps) {
				switch(modifier.getOperation()) {
					case ADDITION:
						currentValue += modifier.getValue();
						break;
					case MULTIPLY_BASE:
						currentValue += baseValue * modifier.getValue();
						break;
					case MULTIPLY_TOTAL:
						currentValue *= modifier.getValue();
						break;
				}
			}
			return currentValue;
		}
		return baseValue;
	}
}

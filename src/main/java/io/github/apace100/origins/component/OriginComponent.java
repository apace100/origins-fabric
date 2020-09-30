package io.github.apace100.origins.component;

import com.google.common.collect.Lists;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.List;

public interface OriginComponent extends EntitySyncedComponent {

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

	static void sync(PlayerEntity player) {
		ModComponents.ORIGIN.get(player).sync();
	}

	static <T extends Power> List<T> getPowers(Entity entity, Class<T> powerClass) {
		if(entity instanceof PlayerEntity) {
			return ModComponents.ORIGIN.get(entity).getPowers(powerClass);
		}
		return Lists.newArrayList();
	}
}

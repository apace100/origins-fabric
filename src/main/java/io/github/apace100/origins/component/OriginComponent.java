package io.github.apace100.origins.component;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public interface OriginComponent extends EntitySyncedComponent {

	boolean hasOrigin();

	Origin getOrigin();

	boolean hadOriginBefore();

	boolean hasPower(PowerType<?> powerType);
	<T extends Power> T getPower(PowerType<T> powerType);
	List<Power> getPowers();
	<T extends Power> List<T> getPowers(Class<T> powerClass);
	<T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive);

	void setOrigin(Origin origin);

	static void sync(PlayerEntity player) {
		ModComponents.ORIGIN.get(player).sync();
	}
}

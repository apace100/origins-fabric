package io.github.apace100.origins.component;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;

import java.util.Collection;
import java.util.List;

public interface OriginComponent extends EntitySyncedComponent {

	boolean hasOrigin();

	Origin getOrigin();

	boolean hasPower(PowerType<?> powerType);
	<T extends Power> T getPower(PowerType<T> powerType);
	<T extends Power> List<T> getPowers(Class<T> powerClass);

	void setOrigin(Origin origin);

}

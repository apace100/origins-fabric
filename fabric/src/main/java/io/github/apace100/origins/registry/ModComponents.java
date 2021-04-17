package io.github.apace100.origins.registry;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import io.github.apace100.origins.component.FabricPlayerOriginComponent;
import io.github.apace100.origins.registry.fabric.ModComponentsArchitecturyImpl;

/**
 * This class exists for backward compatibility purposes and only for fabric.
 * @deprecated Use {@link ModComponentsArchitectury} instead.
 */
@Deprecated
public class ModComponents {
	@Deprecated
	public static final ComponentKey<FabricPlayerOriginComponent> ORIGIN = ModComponentsArchitecturyImpl.ORIGIN;
}

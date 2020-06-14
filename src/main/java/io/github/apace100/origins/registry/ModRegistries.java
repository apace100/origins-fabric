package io.github.apace100.origins.registry;

import io.github.apace100.origins.mixin.RegistryAccessor;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class ModRegistries {

    public static final RegistryKey<Registry<PowerType<?>>> POWER_TYPE_KEY;
    public static final Registry<PowerType<?>> POWER_TYPE;

    public static final RegistryKey<Registry<Origin>> ORIGIN_KEY;
    public static final Registry<Origin> ORIGIN;

    static {
        POWER_TYPE_KEY = RegistryAccessor.callCreateRegistryKey("power_type");
        POWER_TYPE = RegistryAccessor.callCreate(POWER_TYPE_KEY, () -> PowerTypes.WATER_BREATHING);

        ORIGIN_KEY = RegistryAccessor.callCreateRegistryKey("origin");
        ORIGIN = RegistryAccessor.callCreate(ORIGIN_KEY, () -> Origin.EMPTY);
    }
}

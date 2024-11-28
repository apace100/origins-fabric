package io.github.apace100.origins.power.type;

import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.origins.Origins;
import net.minecraft.registry.Registry;

public class OriginsPowerTypes {

    public static final PowerConfiguration<ConduitPowerOnLandPowerType> CONDUIT_POWER_ON_LAND = register(PowerConfiguration.conditionedSimple(Origins.identifier("conduit_power_on_land"), ConduitPowerOnLandPowerType::new));
    public static final PowerConfiguration<LikeWaterPowerType> LIKE_WATER = register(PowerConfiguration.conditionedSimple(Origins.identifier("like_water"), LikeWaterPowerType::new));
    public static final PowerConfiguration<OriginsActionOnCallbackPowerType> ACTION_ON_CALLBACK = register(PowerConfiguration.dataFactory(Origins.identifier("action_on_callback"), OriginsActionOnCallbackPowerType.DATA_FACTORY));
    public static final PowerConfiguration<ScareCreepersPowerType> SCARE_CREEPERS = register(PowerConfiguration.conditionedSimple(Origins.identifier("scare_creepers"), ScareCreepersPowerType::new));
    public static final PowerConfiguration<WaterBreathingPowerType> WATER_BREATHING = register(PowerConfiguration.conditionedSimple(Origins.identifier("water_breathing"), WaterBreathingPowerType::new));
    public static final PowerConfiguration<WaterVisionPowerType> WATER_VISION = register(PowerConfiguration.conditionedSimple(Origins.identifier("water_vision"), WaterVisionPowerType::new));

    public static void register() {

    }

    public static <T extends PowerType> PowerConfiguration<T> register(PowerConfiguration<T> config) {

		//noinspection unchecked
		PowerConfiguration<PowerType> casted = (PowerConfiguration<PowerType>) config;
        Registry.register(ApoliRegistries.POWER_TYPE, casted.id(), casted);

        return config;

    }

}

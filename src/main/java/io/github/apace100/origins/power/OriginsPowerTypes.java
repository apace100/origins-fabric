package io.github.apace100.origins.power;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.PowerFactorySupplier;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.origins.Origins;
import net.minecraft.registry.Registry;

public class OriginsPowerTypes {

    @Deprecated(forRemoval = true)
    public static final PowerType<?> LIKE_WATER = new PowerTypeReference<>(Origins.identifier("like_water"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> WATER_BREATHING = new PowerTypeReference<>(Origins.identifier("water_breathing"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> SCARE_CREEPERS = new PowerTypeReference<>(Origins.identifier("scare_creepers"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> WATER_VISION = new PowerTypeReference<>(Origins.identifier("water_vision"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("no_cobweb_slowdown"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> MASTER_OF_WEBS_NO_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("master_of_webs_no_slowdown"));
    @Deprecated(forRemoval = true)
    public static final PowerType<?> CONDUIT_POWER_ON_LAND = new PowerTypeReference<>(Origins.identifier("conduit_power_on_land"));

    public static void register() {
        register(OriginsCallbackPower::createFactory);
        register(LikeWaterPower.createSimpleFactory(LikeWaterPower::new, Origins.identifier("like_water")));
        register(WaterBreathingPower.createSimpleFactory(WaterBreathingPower::new, Origins.identifier("water_breathing")));
        register(ScareCreepersPower.createSimpleFactory(ScareCreepersPower::new, Origins.identifier("scare_creepers")));
        register(WaterVisionPower.createSimpleFactory(WaterVisionPower::new, Origins.identifier("water_vision")));
        register(ConduitPowerOnLandPower.createSimpleFactory(ConduitPowerOnLandPower::new, Origins.identifier("conduit_power_on_land")));
    }

    private static void register(PowerFactory<?> serializer) {
        Registry.register(ApoliRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }

    private static void register(PowerFactorySupplier<?> supplier) {
        register(supplier.createFactory());
    }

}

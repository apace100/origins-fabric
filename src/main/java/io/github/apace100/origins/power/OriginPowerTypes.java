package io.github.apace100.origins.power;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.origins.Origins;

public class OriginPowerTypes {

    public static final PowerType<?> LIKE_WATER = new PowerTypeReference<>(Origins.identifier("like_water"));
    public static final PowerType<?> WATER_BREATHING = new PowerTypeReference<>(Origins.identifier("water_breathing"));
    public static final PowerType<?> SCARE_CREEPERS = new PowerTypeReference<>(Origins.identifier("scare_creepers"));
    public static final PowerType<?> AQUA_AFFINITY = new PowerTypeReference<>(Origins.identifier("aqua_affinity"));
    public static final PowerType<?> NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("no_cobweb_slowdown"));
    public static final PowerType<?> MASTER_OF_WEBS_NO_SLOWDOWN = new PowerTypeReference<>(Origins.identifier("master_of_webs_no_slowdown"));
}

package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PowerTypes {

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<Power> WATER_VISION;
    public static final PowerType<FloatPower> SWIM_SPEED;
    public static final PowerType<Power> LIKE_WATER;
    public static final PowerType<SetEntityGroupPower> AQUATIC;

    public static final PowerType<AttributePower> FRAGILE;
    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<SetEntityGroupPower> ARTHROPOD;

    static {
        WATER_BREATHING = register("water_breathing", new PowerType<>((type, player) -> new Power(type, player)));
        AQUA_AFFINITY = register("aqua_affinity", new PowerType<>((type, player) -> new Power(type, player)));
        WATER_VISION = register("water_vision", new PowerType<>((type, player) -> new Power(type, player)));
        SWIM_SPEED = register("swim_speed", new PowerType<>((type, player) -> new FloatPower(type, player, 0.04F)));
        LIKE_WATER = register("like_water", new PowerType<>((type, player) -> new Power(type, player)));
        AQUATIC = register("aquatic", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.AQUATIC)).setHidden());

        FRAGILE = register("fragile", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:fragile", -6.0, EntityAttributeModifier.Operation.ADDITION))));
        WEBBING = register("webbing", new PowerType<>((type, player) -> new CooldownPower(type, player, 6 * 20)));
        ARTHROPOD = register("arthropod", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.ARTHROPOD)).setHidden());

    }

    public static void init() {

    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return Registry.register(ModRegistries.POWER_TYPE, new Identifier(Origins.MODID, path), type);
    }
}

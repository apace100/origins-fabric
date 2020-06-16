package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.registry.ModTags;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PowerTypes {

    public static final PowerType<InvulnerablePower> INVULNERABILITY;

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<Power> WATER_VISION;
    public static final PowerType<FloatPower> SWIM_SPEED;
    public static final PowerType<Power> LIKE_WATER;
    public static final PowerType<SetEntityGroupPower> AQUATIC;

    public static final PowerType<AttributePower> FRAGILE;
    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<Power> CLIMBING;
    public static final PowerType<Power> NO_COBWEB_SLOWDOWN;
    public static final PowerType<PreventItemUsePower> CARNIVORE;
    public static final PowerType<SetEntityGroupPower> ARTHROPOD;

    public static final PowerType<Power> FIRE_IMMUNITY;
    public static final PowerType<NetherSpawnPower> NETHER_SPAWN;
    public static final PowerType<ModifyDamageDealtPower> BURNING_WRATH;
    public static final PowerType<VariableIntPower> WATER_VULNERABILITY;

    public static final PowerType<Power> SLOW_FALLING;
    public static final PowerType<AttributePower> TAILWIND;
    public static final PowerType<PreventItemUsePower> VEGETARIAN;
    public static final PowerType<Power> FRESH_AIR;

    static {
        INVULNERABILITY = register("invulnerability", new PowerType<>(InvulnerablePower::new));

        WATER_BREATHING = register("water_breathing", new PowerType<>(Power::new));
        AQUA_AFFINITY = register("aqua_affinity", new PowerType<>(Power::new));
        WATER_VISION = register("water_vision", new PowerType<>(Power::new));
        SWIM_SPEED = register("swim_speed", new PowerType<>((type, player) -> new FloatPower(type, player, 0.04F)));
        LIKE_WATER = register("like_water", new PowerType<>(Power::new));
        AQUATIC = register("aquatic", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.AQUATIC)).setHidden());

        FRAGILE = register("fragile", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:fragile", -6.0, EntityAttributeModifier.Operation.ADDITION))));
        WEBBING = register("webbing", new PowerType<>((type, player) -> new CooldownPower(type, player, 6 * 20)));
        CLIMBING = register("climbing", new PowerType<>(Power::new));
        NO_COBWEB_SLOWDOWN = register("no_cobweb_slowdown", new PowerType<>(Power::new).setHidden());
        CARNIVORE = register("carnivore", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, (stack -> stack.isFood() && !stack.getItem().isIn(ModTags.MEAT)))));
        ARTHROPOD = register("arthropod", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.ARTHROPOD)).setHidden());

        FIRE_IMMUNITY = register("fire_immunity", new PowerType<>(Power::new));
        NETHER_SPAWN = register("nether_spawn", new PowerType<>(NetherSpawnPower::new));
        BURNING_WRATH = register("burning_wrath", new PowerType<>((type, player) -> new ModifyDamageDealtPower(type, player, (p, s) -> p.isOnFire(), dmg -> dmg + 3.0F)));
        WATER_VULNERABILITY = register("water_vulnerability", new PowerType<>((type, player) -> new VariableIntPower(type, player, 20, 0, 20)));

        SLOW_FALLING = register("slow_falling", new PowerType<>(Power::new));
        TAILWIND = register("tailwind", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier("power_type:tailwind", 0.02, EntityAttributeModifier.Operation.ADDITION))));
        VEGETARIAN = register("vegetarian", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, Ingredient.fromTag(ModTags.MEAT))));
        FRESH_AIR = register("fresh_air", new PowerType<>(Power::new));
    }

    public static void init() {

    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return Registry.register(ModRegistries.POWER_TYPE, new Identifier(Origins.MODID, path), type);
    }
}

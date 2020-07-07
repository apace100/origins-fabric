package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.registry.ModTags;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PowerTypes {

    public static final PowerType<InvulnerablePower> INVULNERABILITY;

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<NightVisionPower> WATER_VISION;
    public static final PowerType<FloatPower> SWIM_SPEED;
    public static final PowerType<Power> LIKE_WATER;
    public static final PowerType<SetEntityGroupPower> AQUATIC;

    public static final PowerType<AttributePower> FRAGILE;
    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<TogglePower> CLIMBING;
    public static final PowerType<Power> NO_COBWEB_SLOWDOWN;
    public static final PowerType<PreventItemUsePower> CARNIVORE;
    public static final PowerType<SetEntityGroupPower> ARTHROPOD;

    public static final PowerType<Power> FIRE_IMMUNITY;
    public static final PowerType<NetherSpawnPower> NETHER_SPAWN;
    public static final PowerType<ModifyDamageDealtPower> BURNING_WRATH;
    public static final PowerType<WaterVulnerabilityPower> WATER_VULNERABILITY;
    public static final PowerType<Power> HOTBLOODED;
    public static final PowerType<ParticlePower> FLAME_PARTICLES;

    public static final PowerType<Power> SLOW_FALLING;
    public static final PowerType<AttributePower> TAILWIND;
    public static final PowerType<PreventItemUsePower> VEGETARIAN;
    public static final PowerType<Power> FRESH_AIR;

    public static final PowerType<TogglePower> PHASING;
    public static final PowerType<TogglePower> INVISIBILITY;
    public static final PowerType<TogglePower> HUNGER_OVER_TIME;
    public static final PowerType<TogglePower> BURN_IN_DAYLIGHT;

    public static final PowerType<Power> FALL_IMMUNITY;
    public static final PowerType<Power> SPRINT_JUMP;
    public static final PowerType<Power> WEAK_ARMS;
    public static final PowerType<Power> SCARE_CREEPERS;
    public static final PowerType<AttributePower> NINE_LIVES;
    public static final PowerType<NightVisionPower> CAT_VISION;

    public static final PowerType<ActiveCooldownPower> LAUNCH_INTO_AIR;
    public static final PowerType<Power> ELYTRA;
    public static final PowerType<Power> LIGHT_ARMOR;
    public static final PowerType<StackingStatusEffectPower> CLAUSTROPHOBIA;
    public static final PowerType<ModifyDamageTakenPower> MORE_KINETIC_DAMAGE;
    public static final PowerType<ModifyDamageDealtPower> AERIAL_COMBATANT;

    static {
        INVULNERABILITY = register("invulnerability", new PowerType<>(InvulnerablePower::new));

        WATER_BREATHING = register("water_breathing", new PowerType<>(Power::new));
        AQUA_AFFINITY = register("aqua_affinity", new PowerType<>(Power::new));
        WATER_VISION = register("water_vision", new PowerType<>((type, player) -> (NightVisionPower)new NightVisionPower(type, player).addCondition(p -> p.isSubmergedIn(FluidTags.WATER))));
        SWIM_SPEED = register("swim_speed", new PowerType<>((type, player) -> new FloatPower(type, player, 0.04F)));
        LIKE_WATER = register("like_water", new PowerType<>(Power::new));
        AQUATIC = register("aquatic", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.AQUATIC)).setHidden());

        FRAGILE = register("fragile", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:fragile", -6.0, EntityAttributeModifier.Operation.ADDITION))));
        WEBBING = register("webbing", new PowerType<>((type, player) -> new CooldownPower(type, player, 6 * 20, 5)));
        CLIMBING = register("climbing", new PowerType<>((type, player) -> new TogglePower(type, player, true)));
        NO_COBWEB_SLOWDOWN = register("no_cobweb_slowdown", new PowerType<>(Power::new).setHidden());
        CARNIVORE = register("carnivore", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, (stack -> stack.isFood() && !stack.getItem().isIn(ModTags.MEAT)))));
        ARTHROPOD = register("arthropod", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.ARTHROPOD)).setHidden());

        FIRE_IMMUNITY = register("fire_immunity", new PowerType<>(Power::new));
        NETHER_SPAWN = register("nether_spawn", new PowerType<>(NetherSpawnPower::new));
        BURNING_WRATH = register("burning_wrath", new PowerType<>((type, player) -> new ModifyDamageDealtPower(type, player, (p, s) -> p.isOnFire(), dmg -> dmg + 3.0F)));
        WATER_VULNERABILITY = register("water_vulnerability", new PowerType<>((type, player) -> new WaterVulnerabilityPower(type, player, 20, 0,20)));
        HOTBLOODED = register("hotblooded", new PowerType<>(Power::new));
        FLAME_PARTICLES = register("flame_particles", new PowerType<>((type, player) -> new ParticlePower(type, player, ParticleTypes.FLAME, 4)).setHidden());

        SLOW_FALLING = register("slow_falling", new PowerType<>(Power::new));
        TAILWIND = register("tailwind", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier("power_type:tailwind", 0.02, EntityAttributeModifier.Operation.ADDITION))));
        VEGETARIAN = register("vegetarian", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, Ingredient.fromTag(ModTags.MEAT))));
        FRESH_AIR = register("fresh_air", new PowerType<>(Power::new));

        PHASING = register("phasing", new PowerType<>(TogglePower::new));
        INVISIBILITY = register("invisibility", new PowerType<>(TogglePower::new));
        HUNGER_OVER_TIME = register("hunger_over_time", new PowerType<>(TogglePower::new));
        BURN_IN_DAYLIGHT = register("burn_in_daylight", new PowerType<>((type, player) -> new TogglePower(type, player, true)));

        FALL_IMMUNITY = register("fall_immunity", new PowerType<>(Power::new));
        SPRINT_JUMP = register("sprint_jump", new PowerType<>(Power::new));
        WEAK_ARMS = register("weak_arms", new PowerType<>(Power::new));
        SCARE_CREEPERS = register("scare_creepers", new PowerType<>(Power::new));
        NINE_LIVES = register("nine_lives", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:nine_lives", -2.0, EntityAttributeModifier.Operation.ADDITION))));
        CAT_VISION = register("cat_vision", new PowerType<>((type, player) -> (NightVisionPower)new NightVisionPower(type, player, 0.4F).addCondition(p -> !p.isSubmergedIn(FluidTags.WATER))));

        LAUNCH_INTO_AIR = register("launch_into_air", new PowerType<>((type, player) -> new ActiveCooldownPower(type, player, 20 * 30, 4, p -> {
            p.addVelocity(0, 2, 0);
            p.velocityModified = true;
            p.world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PARROT_FLY, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
            for(int i = 0; i < 4; ++i) {
                ((ServerWorld)p.world).spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getRandomBodyY(), player.getZ(), 8, p.getRandom().nextGaussian(), 0.0D, p.getRandom().nextGaussian(), 0.5);
            }
        })));
        ELYTRA = register("elytra", new PowerType<>(Power::new));
        LIGHT_ARMOR = register("light_armor", new PowerType<>(Power::new));
        AERIAL_COMBATANT = register("aerial_combatant", new PowerType<>((type, player) -> new ModifyDamageDealtPower(type, player, (p, s) -> p.isFallFlying(), dmg -> dmg * 2.5F)));
        CLAUSTROPHOBIA = register("claustrophobia", new PowerType<>((type, player) -> (StackingStatusEffectPower)new StackingStatusEffectPower(type, player, -20, 361, 10).addEffect(StatusEffects.WEAKNESS).addEffect(StatusEffects.SLOWNESS).addCondition(p -> !p.world.doesNotCollide(p.getBoundingBox().stretch(0, 2, 0)))));
        MORE_KINETIC_DAMAGE = register("more_kinetic_damage", new PowerType<>((type, player) -> new ModifyDamageTakenPower(type, player, (p, s) -> s == DamageSource.FALL || s == DamageSource.FLY_INTO_WALL, dmg -> dmg * 1.5F)));
    }

    public static void init() {

    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return Registry.register(ModRegistries.POWER_TYPE, new Identifier(Origins.MODID, path), type);
    }
}

package io.github.apace100.origins.power;

import com.google.gson.*;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {
    //public static final PowerType<InvulnerablePower> INVULNERABILITY;

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<ToggleNightVisionPower> WATER_VISION;
    //public static final PowerType<FloatPower> SWIM_SPEED;
    public static final PowerType<Power> LIKE_WATER;
    //public static final PowerType<SetEntityGroupPower> AQUATIC;

    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<TogglePower> CLIMBING;
    public static final PowerType<Power> NO_COBWEB_SLOWDOWN;

//    public static final PowerType<InvulnerablePower> FIRE_IMMUNITY;
//    public static final PowerType<NetherSpawnPower> NETHER_SPAWN;
    //public static final PowerType<ModifyDamageDealtPower> BURNING_WRATH;
    //public static final PowerType<WaterVulnerabilityPower> WATER_VULNERABILITY;
    //public static final PowerType<Power> HOTBLOODED;
    //public static final PowerType<ParticlePower> FLAME_PARTICLES;

    public static final PowerType<Power> SLOW_FALLING;
//    public static final PowerType<AttributePower> TAILWIND;
//    public static final PowerType<PreventItemUsePower> VEGETARIAN;
//    public static final PowerType<Power> FRESH_AIR;

    //public static final PowerType<TogglePower> PHASING;
    //public static final PowerType<TogglePower> INVISIBILITY;
    //public static final PowerType<TogglePower> HUNGER_OVER_TIME;
    //public static final PowerType<TogglePower> BURN_IN_DAYLIGHT;
//    public static final PowerType<ModelColorPower> TRANSLUCENT;

    //public static final PowerType<InvulnerablePower> FALL_IMMUNITY;
    //public static final PowerType<Power> SPRINT_JUMP;
//    public static final PowerType<Power> WEAK_ARMS;
    public static final PowerType<Power> SCARE_CREEPERS;
//    public static final PowerType<AttributePower> NINE_LIVES;
//    public static final PowerType<NightVisionPower> CAT_VISION;

    //public static final PowerType<ActiveCooldownPower> LAUNCH_INTO_AIR;
    //public static final PowerType<Power> ELYTRA;
    //public static final PowerType<RestrictArmorPower> LIGHT_ARMOR;
    //public static final PowerType<StackingStatusEffectPower> CLAUSTROPHOBIA;
    //public static final PowerType<ModifyDamageTakenPower> MORE_KINETIC_DAMAGE;
    //public static final PowerType<ModifyDamageDealtPower> AERIAL_COMBATANT;

    public static final PowerType<PreventItemUsePower> PUMPKIN_HATE;
    //public static final PowerType<ActiveCooldownPower> THROW_ENDER_PEARL;
    //public static final PowerType<AttributePower> EXTRA_REACH;
    //public static final PowerType<ParticlePower> ENDER_PARTICLES;

    //public static final PowerType<InventoryPower> INVENTORY;
    //public static final PowerType<AttributePower> NATURAL_ARMOR;
    //public static final PowerType<PreventItemUsePower> NO_SHIELD;
    //public static final PowerType<ModifyExhaustionPower> MORE_EXHAUSTION;
    //public static final PowerType<Power> STRONG_ARMS;

    // Unused Powers
    //public static final PowerType<Power> LAVA_STRIDER;
    //public static final PowerType<PreventItemUsePower> NO_RANGED_WEAPONS;
    static {
        //INVULNERABILITY = register("invulnerability", new PowerType<>((type, player) -> new InvulnerablePower(type, player, ds -> true)));

        WATER_BREATHING = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_breathing")); //register("water_breathing", new PowerType<>(Power::new));
        AQUA_AFFINITY = new PowerTypeReference<>(new Identifier(Origins.MODID, "aqua_affinity")); //register("aqua_affinity", new PowerType<>(Power::new));
        WATER_VISION = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_vision"));//register("water_vision", new PowerType<>((type, player) -> (ToggleNightVisionPower)new ToggleNightVisionPower(type, player).addCondition(p -> p.isSubmergedIn(FluidTags.WATER))));
        //SWIM_SPEED = register("swim_speed", new PowerType<>((type, player) -> new FloatPower(type, player, 0.04F)));
        LIKE_WATER = new PowerTypeReference<>(new Identifier(Origins.MODID, "like_water"));//register("like_water", new PowerType<>(Power::new));
        //AQUATIC = register("aquatic", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.AQUATIC)).setHidden());

        //FRAGILE = register("fragile", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:fragile", -6.0, EntityAttributeModifier.Operation.ADDITION))));
        WEBBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "webbing")); //register("webbing", new PowerType<>((type, player) -> new CooldownPower(type, player, 6 * 20, 5)));
        CLIMBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "climbing")); //register("climbing", new PowerType<>((type, player) -> new TogglePower(type, player, true)));
        NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(new Identifier(Origins.MODID, "no_cobweb_slowdown"));//register("no_cobweb_slowdown", new PowerType<>(Power::new).setHidden());
        //CARNIVORE = register("carnivore", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, (stack -> stack.isFood() && !(stack.getItem().getFoodComponent().isMeat() || stack.getItem().isIn(ModTags.MEAT))))));
        //ARTHROPOD = register("arthropod", new PowerType<>((type, player) -> new SetEntityGroupPower(type, player, EntityGroup.ARTHROPOD)).setHidden());

        //FIRE_IMMUNITY = register("fire_immunity", new PowerType<>((type, player) -> new InvulnerablePower(type, player, DamageSource::isFire)));
        //NETHER_SPAWN = register("nether_spawn", new PowerType<>(NetherSpawnPower::new));
        //BURNING_WRATH = register("burning_wrath", new PowerType<>((type, player) -> new ModifyDamageDealtPower(type, player, (p, s) -> p.isOnFire(), dmg -> dmg + 3.0F)));
        //WATER_VULNERABILITY = register("water_vulnerability", new PowerType<>((type, player) -> new WaterVulnerabilityPower(type, player, 20, 0,20)));
        //HOTBLOODED = new PowerTypeReference<>(new Identifier(Origins.MODID, "hotblooded"));//register("hotblooded", new PowerType<>(Power::new));
        //FLAME_PARTICLES = register("flame_particles", new PowerType<>((type, player) -> new ParticlePower(type, player, ParticleTypes.FLAME, 4)).setHidden());

        SLOW_FALLING = new PowerTypeReference<>(new Identifier(Origins.MODID, "slow_falling"));//register("slow_falling", new PowerType<>(Power::new));
        //TAILWIND = register("tailwind", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier("power_type:tailwind", 0.02, EntityAttributeModifier.Operation.ADDITION))));
        //VEGETARIAN = register("vegetarian", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, (stack -> stack.isFood() && (stack.getItem().getFoodComponent().isMeat() || stack.getItem().isIn(ModTags.MEAT))))));
        //FRESH_AIR = register("fresh_air", new PowerType<>(Power::new));

        //PHASING = register("phasing", new PowerType<>(TogglePower::new));
        //INVISIBILITY = register("invisibility", new PowerType<>(TogglePower::new));
        //HUNGER_OVER_TIME = register("hunger_over_time", new PowerType<>(TogglePower::new));
        //BURN_IN_DAYLIGHT = register("burn_in_daylight", new PowerType<>((type, player) -> new TogglePower(type, player, true)));
        //TRANSLUCENT = register("translucent", new PowerType<>((type, player) -> new ModelColorPower(type, player, 0.5F)));

        //FALL_IMMUNITY = register("fall_immunity", new PowerType<>((type, player) -> new InvulnerablePower(type, player, ds -> ds == DamageSource.FALL)));
        //SPRINT_JUMP = register("sprint_jump", new PowerType<>(Power::new));
        //WEAK_ARMS = register("weak_arms", new PowerType<>(Power::new));
        SCARE_CREEPERS = new PowerTypeReference<>(new Identifier(Origins.MODID, "scare_creepers"));//register("scare_creepers", new PowerType<>(Power::new));
        //NINE_LIVES = register("nine_lives", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier("power_type:nine_lives", -2.0, EntityAttributeModifier.Operation.ADDITION))));
        //CAT_VISION = register("cat_vision", new PowerType<>((type, player) -> (NightVisionPower)new NightVisionPower(type, player, 0.4F).addCondition(p -> !p.isSubmergedIn(FluidTags.WATER))));

        /*LAUNCH_INTO_AIR = register("launch_into_air", new PowerType<>((type, player) -> new ActiveCooldownPower(type, player, 20 * 30, 4, p -> {
            if(!p.world.isClient) {
                p.addVelocity(0, 2, 0);
                p.velocityModified = true;
                p.world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PARROT_FLY, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
                for(int i = 0; i < 4; ++i) {
                    ((ServerWorld)p.world).spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getRandomBodyY(), player.getZ(), 8, p.getRandom().nextGaussian(), 0.0D, p.getRandom().nextGaussian(), 0.5);
                }
            }
        })));*/
        //ELYTRA = register("elytra", new PowerType<>(Power::new));
        //LIGHT_ARMOR = register("light_armor", new PowerType<>((type, player) -> new RestrictArmorPower(type, player, (is, pl, slt) -> is.getItem() instanceof ArmorItem && ((ArmorItem)is.getItem()).getProtection() > Constants.LIGHT_ARMOR_MAX_PROTECTION[slt.getEntitySlotId()])));
        //AERIAL_COMBATANT = register("aerial_combatant", new PowerType<>((type, player) -> new ModifyDamageDealtPower(type, player, (p, s) -> p.isFallFlying(), dmg -> dmg * 2F)));
        //CLAUSTROPHOBIA = register("claustrophobia", new PowerType<>((type, player) -> (StackingStatusEffectPower)new StackingStatusEffectPower(type, player, -20, 361, 10).addEffect(StatusEffects.WEAKNESS).addEffect(StatusEffects.SLOWNESS).addCondition(p -> p.world.getBlockCollisions(p, p.getBoundingBox().offset(0, p.getBoundingBox().getYLength(), 0)).findAny().isPresent())));
        //MORE_KINETIC_DAMAGE = register("more_kinetic_damage", new PowerType<>((type, player) -> new ModifyDamageTakenPower(type, player, (p, s) -> s == DamageSource.FALL || s == DamageSource.FLY_INTO_WALL, dmg -> dmg * 1.5F)));

        PUMPKIN_HATE = new PowerTypeReference<>(new Identifier(Origins.MODID, "pumpkin_hate"));//register("pumpkin_hate", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, stack -> stack.getItem() == Items.PUMPKIN_PIE)));
        /*THROW_ENDER_PEARL = register("throw_ender_pearl", new PowerType<>((type, player) -> new ActiveCooldownPower(type, player, 30, 6, p -> {
            p.world.playSound((PlayerEntity)null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!p.world.isClient) {
                EnderianPearlEntity enderPearlEntity = new EnderianPearlEntity(p.world, p);
                enderPearlEntity.setProperties(p, p.pitch, p.yaw, 0.0F, 1.5F, 1.0F);
                p.world.spawnEntity(enderPearlEntity);
            }
        })));*/
        //EXTRA_REACH = register("extra_reach", new PowerType<>((type, player) -> new AttributePower(type, player)
        //    .addModifier(ReachEntityAttributes.REACH, new EntityAttributeModifier("power_type:extra_reach", 1.5, EntityAttributeModifier.Operation.ADDITION))
        //    .addModifier(ReachEntityAttributes.ATTACK_RANGE, new EntityAttributeModifier("power_type:extra_reach", 1.5, EntityAttributeModifier.Operation.ADDITION))));
        //ENDER_PARTICLES = register("ender_particles", new PowerType<>((type, player) -> new ParticlePower(type, player, ParticleTypes.PORTAL, 4)).setHidden());

        //LAVA_STRIDER = register("lava_strider", new PowerType<>(Power::new));

        //INVENTORY = register("shulker_inventory", new PowerType<>((type, player) -> new InventoryPower(type, player, "container.shulker_inventory_power")));
        //NATURAL_ARMOR = register("natural_armor", new PowerType<>((type, player) -> new AttributePower(type, player, EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier("power_type:natural_armor", 8.0, EntityAttributeModifier.Operation.ADDITION))));
        //NO_SHIELD = register("no_shield", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, Ingredient.ofItems(Items.SHIELD))));
        //MORE_EXHAUSTION = register("more_exhaustion", new PowerType<>((type, player) -> new ModifyExhaustionPower(type, player, 1.6F)));
        //STRONG_ARMS = register("strong_arms", new PowerType<>(Power::new));

        //NO_RANGED_WEAPONS = register("no_ranged_weapons", new PowerType<>((type, player) -> new PreventItemUsePower(type, player, Ingredient.fromTag(ModTags.RANGED_WEAPONS))));
    }

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private HashMap<Identifier, Integer> loadingPriorities = new HashMap<>();

    public PowerTypes() {
        super(GSON, "powers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
        PowerTypeRegistry.reset();
        loadingPriorities.clear();
        loader.forEach((id, jel) -> {
            jel.forEach(je -> {
                try {
                    JsonObject jo = je.getAsJsonObject();
                    Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
                    Optional<PowerFactory> optionalFactory = ModRegistries.POWER_FACTORY.getOrEmpty(factoryId);
                    if(!optionalFactory.isPresent()) {
                        throw new JsonSyntaxException("Power type \"" + factoryId.toString() + "\" is not defined.");
                    }
                    PowerFactory.Instance factoryInstance = optionalFactory.get().read(jo);
                    PowerType type = new PowerType(id, factoryInstance);
                    int priority = JsonHelper.getInt(jo, "loading_priority", 0);
                    String name = JsonHelper.getString(jo, "name", "");
                    String description = JsonHelper.getString(jo, "description", "");
                    boolean hidden = JsonHelper.getBoolean(jo, "hidden", false);
                    if(hidden) {
                        type.setHidden();
                    }
                    type.setTranslationKeys(name, description);
                    if(!PowerTypeRegistry.contains(id)) {
                        PowerTypeRegistry.register(id, type);
                        loadingPriorities.put(id, priority);
                    } else {
                        if(loadingPriorities.get(id) < priority) {
                            PowerTypeRegistry.register(id, type);
                            loadingPriorities.put(id, priority);
                        }
                    }
                } catch(Exception e) {
                    Origins.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        loadingPriorities.clear();
        Origins.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Origins.MODID, "powers");
    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return new PowerTypeReference<>(new Identifier(Origins.MODID, path));
        //return PowerTypeRegistry.register(new Identifier(Origins.MODID, path), type);
    }
}

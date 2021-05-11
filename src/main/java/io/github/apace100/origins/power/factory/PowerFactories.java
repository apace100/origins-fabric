package io.github.apace100.origins.power.factory;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.power.factory.action.ActionFactory;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.registry.ModDamageSources;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.*;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PowerFactories {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new PowerFactory<>(Origins.identifier("simple"), new SerializableData(), data -> Power::new).allowCondition());
        register(new PowerFactory<>(Origins.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataType.BOOLEAN, true)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    TogglePower power = new TogglePower(type, player, data.getBoolean("active_by_default"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("attribute"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    AttributePower ap = new AttributePower(type, player);
                    if(data.isPresent("modifier")) {
                        ap.addModifier((AttributedEntityAttributeModifier)data.get("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        List<AttributedEntityAttributeModifier> modifierList = (List<AttributedEntityAttributeModifier>)data.get("modifiers");
                        modifierList.forEach(ap::addModifier);
                    }
                    return ap;
                }));
        register(new PowerFactory<>(Origins.identifier("burn"),
            new SerializableData()
                .add("interval", SerializableDataType.INT)
                .add("burn_duration", SerializableDataType.INT),
            data ->
                (type, player) ->
                    new BurnPower(type, player, data.getInt("interval"), data.getInt("burn_duration")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER),
            data ->
                (type, player) ->
                    new CooldownPower(type, player, data.getInt("cooldown"), (HudRender)data.get("hud_render")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("effect_immunity"),
            new SerializableData()
                .add("effect", SerializableDataType.STATUS_EFFECT, null)
                .add("effects", SerializableDataType.STATUS_EFFECTS, null),
            data ->
                (type, player) -> {
                    EffectImmunityPower power = new EffectImmunityPower(type, player);
                    if(data.isPresent("effect")) {
                        power.addEffect((StatusEffect)data.get("effect"));
                    }
                    if(data.isPresent("effects")) {
                        ((List<StatusEffect>)data.get("effects")).forEach(power::addEffect);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("elytra_flight"),
            new SerializableData()
                .add("render_elytra", SerializableDataType.BOOLEAN),
            data ->
                (type, player) -> new ElytraFlightPower(type, player, data.getBoolean("render_elytra")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("entity_group"),
            new SerializableData()
                .add("group", SerializableDataType.ENTITY_GROUP),
            data ->
                (type, player) -> new SetEntityGroupPower(type, player, (EntityGroup)data.get("group")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("exhaust"),
            new SerializableData()
                .add("interval", SerializableDataType.INT)
                .add("exhaustion", SerializableDataType.FLOAT),
            data ->
                (type, player) -> new ExhaustOverTimePower(type, player, data.getInt("interval"), data.getFloat("exhaustion")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("fire_projectile"),
            new SerializableData()
                .add("cooldown", SerializableDataType.INT)
                .add("count", SerializableDataType.INT, 1)
                .add("speed", SerializableDataType.FLOAT, 1.5F)
                .add("divergence", SerializableDataType.FLOAT, 1F)
                .add("sound", SerializableDataType.SOUND_EVENT, null)
                .add("entity_type", SerializableDataType.ENTITY_TYPE)
                .add("hud_render", SerializableDataType.HUD_RENDER)
                .add("tag", SerializableDataType.NBT, null)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    FireProjectilePower power = new FireProjectilePower(type, player,
                        data.getInt("cooldown"),
                        (HudRender)data.get("hud_render"),
                        (EntityType)data.get("entity_type"),
                        data.getInt("count"),
                        data.getFloat("speed"),
                        data.getFloat("divergence"),
                        (SoundEvent)data.get("sound"),
                        (CompoundTag)data.get("tag"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("inventory"),
            new SerializableData()
                .add("name", SerializableDataType.STRING, "container.inventory")
                .add("drop_on_death", SerializableDataType.BOOLEAN, false)
                .add("drop_on_death_filter", SerializableDataType.ITEM_CONDITION, null)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    InventoryPower power = new InventoryPower(type, player, data.getString("name"), 9,
                        data.getBoolean("drop_on_death"),
                        data.isPresent("drop_on_death_filter") ? (ConditionFactory<ItemStack>.Instance) data.get("drop_on_death_filter") :
                            itemStack -> true);
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("invisibility"),
            new SerializableData()
                .add("render_armor", SerializableDataType.BOOLEAN),
            data ->
                (type, player) -> new InvisibilityPower(type, player, data.getBoolean("render_armor")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("invulnerability"),
            new SerializableData()
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION),
            data ->
                (type, player) -> {
                    ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition =
                        (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition");
                    return new InvulnerablePower(type, player, ds -> damageCondition.test(new Pair<>(ds, null)));
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("launch"),
            new SerializableData()
                .add("cooldown", SerializableDataType.INT)
                .add("speed", SerializableDataType.FLOAT)
                .add("sound", SerializableDataType.SOUND_EVENT, null)
                .add("hud_render", SerializableDataType.HUD_RENDER)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data -> {
                SoundEvent soundEvent = (SoundEvent)data.get("sound");
                return (type, player) -> {
                    ActiveCooldownPower power = new ActiveCooldownPower(type, player,
                        data.getInt("cooldown"),
                        (HudRender) data.get("hud_render"),
                        e -> {
                            if (!e.world.isClient && e instanceof PlayerEntity) {
                                PlayerEntity p = (PlayerEntity) e;
                                p.addVelocity(0, data.getFloat("speed"), 0);
                                p.velocityModified = true;
                                if (soundEvent != null) {
                                    p.world.playSound((PlayerEntity) null, p.getX(), p.getY(), p.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
                                }
                                for (int i = 0; i < 4; ++i) {
                                    ((ServerWorld) p.world).spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getRandomBodyY(), p.getZ(), 8, p.getRandom().nextGaussian(), 0.0D, p.getRandom().nextGaussian(), 0.5);
                                }
                            }
                        });
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                };
            }).allowCondition());
        register(new PowerFactory<>(Origins.identifier("model_color"),
            new SerializableData()
                .add("red", SerializableDataType.FLOAT, 1.0F)
                .add("green", SerializableDataType.FLOAT, 1.0F)
                .add("blue", SerializableDataType.FLOAT, 1.0F)
                .add("alpha", SerializableDataType.FLOAT, 1.0F),
            data ->
                (type, player) ->
                    new ModelColorPower(type, player, data.getFloat("red"), data.getFloat("green"), data.getFloat("blue"), data.getFloat("alpha")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_break_speed"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null)
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifyBreakSpeedPower power = new ModifyBreakSpeedPower(type, player, data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_damage_dealt"),
            new SerializableData()
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("target_condition", SerializableDataType.ENTITY_CONDITION, null)
                .add("self_action", SerializableDataType.ENTITY_ACTION, null)
                .add("target_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyDamageDealtPower power = new ModifyDamageDealtPower(type, player,
                        data.isPresent("damage_condition") ? (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition") : dmg -> true,
                        (ConditionFactory<LivingEntity>.Instance)data.get("target_condition"));
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    if(data.isPresent("self_action")) {
                        power.setSelfAction((ActionFactory<LivingEntity>.Instance)data.get("self_action"));
                    }
                    if(data.isPresent("target_action")) {
                        power.setTargetAction((ActionFactory<LivingEntity>.Instance)data.get("target_action"));
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_damage_taken"),
            new SerializableData()
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("self_action", SerializableDataType.ENTITY_ACTION, null)
                .add("attacker_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyDamageTakenPower power = new ModifyDamageTakenPower(type, player,
                        data.isPresent("damage_condition") ? (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition") : dmg -> true);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    if(data.isPresent("self_action")) {
                        power.setSelfAction((ActionFactory<LivingEntity>.Instance)data.get("self_action"));
                    }
                    if(data.isPresent("attacker_action")) {
                        power.setAttackerAction((ActionFactory<LivingEntity>.Instance)data.get("attacker_action"));
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_exhaustion"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifyExhaustionPower power = new ModifyExhaustionPower(type, player);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_harvest"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null)
                .add("allow", SerializableDataType.BOOLEAN),
            data ->
                (type, player) ->
                    new ModifyHarvestPower(type, player,
                        data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true,
                        data.getBoolean("allow")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_jump"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyJumpPower power = new ModifyJumpPower(type, player, (ActionFactory<Entity>.Instance)data.get("entity_action"));
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_player_spawn"),
                new SerializableData()
                        .add("dimension", SerializableDataType.DIMENSION)
                        .add("dimension_distance_multiplier", SerializableDataType.FLOAT, 0F)
                        .add("biome", SerializableDataType.IDENTIFIER, null)
                        .add("spawn_strategy", SerializableDataType.STRING, "default")
                        .add("structure", SerializableDataType.registry(ClassUtil.castClass(StructureFeature.class), Registry.STRUCTURE_FEATURE), null)
                        .add("respawn_sound", SerializableDataType.SOUND_EVENT, null),
                data ->
                        (type, player) ->
                                new ModifyPlayerSpawnPower(type, player,
                                        (RegistryKey<World>)data.get("dimension"),
                                        data.getFloat("dimension_distance_multiplier"),
                                        data.getId("biome"),
                                        data.getString("spawn_strategy"),
                                        data.isPresent("structure") ? (StructureFeature<?>)data.get("structure") : null,
                                        (SoundEvent)data.get("respawn_sound")))
                .allowCondition());
        register(new PowerFactory<>(Origins.identifier("night_vision"),
            new SerializableData()
                .add("strength", SerializableDataType.FLOAT, 1.0F),
            data ->
                (type, player) ->
                    new NightVisionPower(type, player, data.getFloat("strength")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("particle"),
            new SerializableData()
                .add("particle", SerializableDataType.PARTICLE_TYPE)
                .add("frequency", SerializableDataType.INT),
            data ->
                (type, player) ->
                    new ParticlePower(type, player, (ParticleEffect)data.get("particle"), data.getInt("frequency")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("phasing"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null)
                .add("blacklist", SerializableDataType.BOOLEAN, false)
                .add("render_type", SerializableDataType.enumValue(PhasingPower.RenderType.class), PhasingPower.RenderType.BLINDNESS)
                .add("view_distance", SerializableDataType.FLOAT, 10F)
                .add("phase_down_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) ->
                    new PhasingPower(type, player, data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true,
                        data.getBoolean("blacklist"), (PhasingPower.RenderType)data.get("render_type"), data.getFloat("view_distance"),
                        (ConditionFactory<PlayerEntity>.Instance)data.get("phase_down_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_item_use"),
            new SerializableData()
                .add("item_condition", SerializableDataType.ITEM_CONDITION, null),
            data ->
                (type, player) ->
                    new PreventItemUsePower(type, player, data.isPresent("item_condition") ? (ConditionFactory<ItemStack>.Instance)data.get("item_condition") : item -> true))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_sleep"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null)
                .add("message", SerializableDataType.STRING, "origins.cant_sleep")
                .add("set_spawn_point", SerializableDataType.BOOLEAN, false),
            data ->
                (type, player) ->
                    new PreventSleepPower(type, player,
                        data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true,
                        data.getString("message"), data.getBoolean("set_spawn_point")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("restrict_armor"),
            new SerializableData()
                .add("head", SerializableDataType.ITEM_CONDITION, null)
                .add("chest", SerializableDataType.ITEM_CONDITION, null)
                .add("legs", SerializableDataType.ITEM_CONDITION, null)
                .add("feet", SerializableDataType.ITEM_CONDITION, null),
            data ->
                (type, player) -> {
                    HashMap<EquipmentSlot, Predicate<ItemStack>> restrictions = new HashMap<>();
                    if(data.isPresent("head")) {
                        restrictions.put(EquipmentSlot.HEAD, (ConditionFactory<ItemStack>.Instance)data.get("head"));
                    }
                    if(data.isPresent("chest")) {
                        restrictions.put(EquipmentSlot.CHEST, (ConditionFactory<ItemStack>.Instance)data.get("chest"));
                    }
                    if(data.isPresent("legs")) {
                        restrictions.put(EquipmentSlot.LEGS, (ConditionFactory<ItemStack>.Instance)data.get("legs"));
                    }
                    if(data.isPresent("feet")) {
                        restrictions.put(EquipmentSlot.FEET, (ConditionFactory<ItemStack>.Instance)data.get("feet"));
                    }
                    return new RestrictArmorPower(type, player, restrictions);
                }));

        register(new PowerFactory<>(Origins.identifier("conditioned_restrict_armor"),
            new SerializableData()
                .add("head", SerializableDataType.ITEM_CONDITION, null)
                .add("chest", SerializableDataType.ITEM_CONDITION, null)
                .add("legs", SerializableDataType.ITEM_CONDITION, null)
                .add("feet", SerializableDataType.ITEM_CONDITION, null)
                .add("tick_rate", SerializableDataType.INT, 80),
            data ->
                (type, player) -> {
                    HashMap<EquipmentSlot, Predicate<ItemStack>> restrictions = new HashMap<>();
                    if(data.isPresent("head")) {
                        restrictions.put(EquipmentSlot.HEAD, (ConditionFactory<ItemStack>.Instance)data.get("head"));
                    }
                    if(data.isPresent("chest")) {
                        restrictions.put(EquipmentSlot.CHEST, (ConditionFactory<ItemStack>.Instance)data.get("chest"));
                    }
                    if(data.isPresent("legs")) {
                        restrictions.put(EquipmentSlot.LEGS, (ConditionFactory<ItemStack>.Instance)data.get("legs"));
                    }
                    if(data.isPresent("feet")) {
                        restrictions.put(EquipmentSlot.FEET, (ConditionFactory<ItemStack>.Instance)data.get("feet"));
                    }
                    return new ConditionedRestrictArmorPower(type, player, restrictions, data.getInt("tick_rate"));
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("stacking_status_effect"),
            new SerializableData()
                .add("min_stacks", SerializableDataType.INT)
                .add("max_stacks", SerializableDataType.INT)
                .add("duration_per_stack", SerializableDataType.INT)
                .add("effect", SerializableDataType.STATUS_EFFECT_INSTANCE, null)
                .add("effects", SerializableDataType.STATUS_EFFECT_INSTANCES, null),
            data ->
                (type, player) -> {
                    StackingStatusEffectPower power = new StackingStatusEffectPower(type, player,
                        data.getInt("min_stacks"),
                        data.getInt("max_stacks"),
                        data.getInt("duration_per_stack"));
                    if(data.isPresent("effect")) {
                        power.addEffect((StatusEffectInstance)data.get("effect"));
                    }
                    if(data.isPresent("effects")) {
                        ((List<StatusEffectInstance>)data.get("effects")).forEach(power::addEffect);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_swim_speed"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifySwimSpeedPower power = new ModifySwimSpeedPower(type, player);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("toggle_night_vision"),
            new SerializableData()
                .add("active_by_default", SerializableDataType.BOOLEAN, false)
                .add("strength", SerializableDataType.FLOAT, 1.0F)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    ToggleNightVisionPower power = new ToggleNightVisionPower(type, player, data.getFloat("strength"), data.getBoolean("active_by_default"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("damage_over_time"),
            new SerializableData()
                .add("interval", SerializableDataType.INT)
                .addFunctionedDefault("onset_delay", SerializableDataType.INT, data -> data.getInt("interval"))
                .add("damage", SerializableDataType.FLOAT)
                .addFunctionedDefault("damage_easy", SerializableDataType.FLOAT, data -> data.getFloat("damage"))
                .add("damage_source", SerializableDataType.DAMAGE_SOURCE, ModDamageSources.GENERIC_DOT)
                .add("protection_enchantment", SerializableDataType.ENCHANTMENT, null)
                .add("protection_effectiveness", SerializableDataType.FLOAT, 1.0F),
            data ->
                (type, player) -> new DamageOverTimePower(type, player,
                    data.getInt("onset_delay"),
                    data.getInt("interval"),
                    data.getFloat("damage_easy"),
                    data.getFloat("damage"),
                    (DamageSource)data.get("damage_source"),
                    (Enchantment)data.get("protection_enchantment"),
                    data.getFloat("protection_effectiveness")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("swimming"),
            new SerializableData(), data -> SwimmingPower::new).allowCondition());
        register(new PowerFactory<>(Origins.identifier("fire_immunity"),
            new SerializableData(), data -> FireImmunityPower::new).allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_lava_speed"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifyLavaSpeedPower power = new ModifyLavaSpeedPower(type, player);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("lava_vision"),
            new SerializableData()
                .add("s", SerializableDataType.FLOAT)
                .add("v", SerializableDataType.FLOAT),
            data ->
                (type, player) ->
                    new LavaVisionPower(type, player, data.getFloat("s"), data.getFloat("v")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("conditioned_attribute"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("tick_rate", SerializableDataType.INT, 20),
            data ->
                (type, player) -> {
                    ConditionedAttributePower ap = new ConditionedAttributePower(type, player, data.getInt("tick_rate"));
                    if(data.isPresent("modifier")) {
                        ap.addModifier((AttributedEntityAttributeModifier)data.get("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        List<AttributedEntityAttributeModifier> modifierList = (List<AttributedEntityAttributeModifier>)data.get("modifiers");
                        modifierList.forEach(ap::addModifier);
                    }
                    return ap;
                }).allowCondition());
        register(new PowerFactory<>(Origins.identifier("active_self"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER)
                .add("key", SerializableDataType.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    ActiveCooldownPower power = new ActiveCooldownPower(type, player, data.getInt("cooldown"), (HudRender)data.get("hud_render"),
                        (ActionFactory<Entity>.Instance)data.get("entity_action"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("action_over_time"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null)
                .add("rising_action", SerializableDataType.ENTITY_ACTION, null)
                .add("falling_action", SerializableDataType.ENTITY_ACTION, null)
                .add("interval", SerializableDataType.INT),
            data ->
                (type, player) -> new ActionOverTimePower(type, player, data.getInt("interval"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"), (ActionFactory<Entity>.Instance)data.get("rising_action"), (ActionFactory<Entity>.Instance)data.get("falling_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("self_action_when_hit"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER, HudRender.DONT_RENDER),
            data ->
                (type, player) -> new SelfActionWhenHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("attacker_action_when_hit"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER, HudRender.DONT_RENDER),
            data ->
                (type, player) -> new AttackerActionWhenHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("self_action_on_hit"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER, HudRender.DONT_RENDER)
                .add("target_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new SelfActionOnHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<LivingEntity>.Instance)data.get("target_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("target_action_on_hit"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER, HudRender.DONT_RENDER)
                .add("target_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new TargetActionOnHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<LivingEntity>.Instance)data.get("target_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("starting_equipment"),
            new SerializableData()
                .add("stack", SerializableDataType.POSITIONED_ITEM_STACK, null)
                .add("stacks", SerializableDataType.POSITIONED_ITEM_STACKS, null)
                .add("recurrent", SerializableDataType.BOOLEAN, false),
            data ->
                (type, player) -> {
                    StartingEquipmentPower power = new StartingEquipmentPower(type, player);
                    if(data.isPresent("stack")) {
                        Pair<Integer, ItemStack> stack = (Pair<Integer, ItemStack>)data.get("stack");
                        int slot = stack.getLeft();
                        if(slot > Integer.MIN_VALUE) {
                            power.addStack(stack.getLeft(), stack.getRight());
                        } else {
                            power.addStack(stack.getRight());
                        }
                    }
                    if(data.isPresent("stacks")) {
                        ((List<Pair<Integer, ItemStack>>)data.get("stacks"))
                            .forEach(integerItemStackPair -> {
                                int slot = integerItemStackPair.getLeft();
                                if(slot > Integer.MIN_VALUE) {
                                    power.addStack(integerItemStackPair.getLeft(), integerItemStackPair.getRight());
                                } else {
                                    power.addStack(integerItemStackPair.getRight());
                                }
                            });
                    }
                    power.setRecurrent(data.getBoolean("recurrent"));
                    return power;
                }));
        register(new PowerFactory<>(Origins.identifier("action_on_callback"),
            new SerializableData()
                .add("entity_action_respawned", SerializableDataType.ENTITY_ACTION, null)
                .add("entity_action_removed", SerializableDataType.ENTITY_ACTION, null)
                .add("entity_action_chosen", SerializableDataType.ENTITY_ACTION, null)
                .add("entity_action_lost", SerializableDataType.ENTITY_ACTION, null)
                .add("entity_action_added", SerializableDataType.ENTITY_ACTION, null)
                .add("execute_chosen_when_orb", SerializableDataType.BOOLEAN, true),
            data ->
                (type, player) -> new ActionOnCallbackPower(type, player,
                    (ActionFactory<Entity>.Instance)data.get("entity_action_respawned"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action_removed"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action_chosen"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action_lost"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action_added"),
                    data.getBoolean("execute_chosen_when_orb")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("walk_on_fluid"),
            new SerializableData()
                .add("fluid", SerializableDataType.FLUID_TAG),
            data ->
                (type, player) -> new WalkOnFluidPower(type, player, (Tag<Fluid>)data.get("fluid")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("shader"),
            new SerializableData()
                .add("shader", SerializableDataType.IDENTIFIER),
            data ->
                (type, player) -> new ShaderPower(type, player, data.getId("shader")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("shaking"),
            new SerializableData(), data -> (BiFunction<PowerType<Power>, PlayerEntity, Power>) ShakingPower::new)
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("disable_regen"),
            new SerializableData(), data -> DisableRegenPower::new).allowCondition());
        register(new PowerFactory<>(Origins.identifier("resource"),
            new SerializableData()
                .add("min", SerializableDataType.INT)
                .add("max", SerializableDataType.INT)
                .addFunctionedDefault("start_value", SerializableDataType.INT, data -> data.getInt("min"))
                .add("hud_render", SerializableDataType.HUD_RENDER)
                .add("min_action", SerializableDataType.ENTITY_ACTION, null)
                .add("max_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) ->
                    new ResourcePower(type, player,
                        (HudRender)data.get("hud_render"),
                        data.getInt("start_value"),
                        data.getInt("min"),
                        data.getInt("max"),
                        (ActionFactory<Entity>.Instance)data.get("min_action"),
                        (ActionFactory<Entity>.Instance)data.get("max_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_food"),
            new SerializableData()
                .add("item_condition", SerializableDataType.ITEM_CONDITION, null)
                .add("food_modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("food_modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("saturation_modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("saturation_modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    List<EntityAttributeModifier> foodModifiers = new LinkedList<>();
                    List<EntityAttributeModifier> saturationModifiers = new LinkedList<>();
                    if(data.isPresent("food_modifier")) {
                        foodModifiers.add((EntityAttributeModifier)data.get("food_modifier"));
                    }
                    if(data.isPresent("food_modifiers")) {
                        List<EntityAttributeModifier> modifierList = (List<EntityAttributeModifier>)data.get("food_modifiers");
                        foodModifiers.addAll(modifierList);
                    }
                    if(data.isPresent("saturation_modifier")) {
                        saturationModifiers.add((EntityAttributeModifier)data.get("saturation_modifier"));
                    }
                    if(data.isPresent("saturation_modifiers")) {
                        List<EntityAttributeModifier> modifierList = (List<EntityAttributeModifier>)data.get("saturation_modifiers");
                        saturationModifiers.addAll(modifierList);
                    }
                    return new ModifyFoodPower(type, player, data.isPresent("item_condition") ? (ConditionFactory<ItemStack>.Instance)data.get("item_condition") : stack -> true,
                        foodModifiers, saturationModifiers, (ActionFactory<Entity>.Instance)data.get("entity_action"));
                }).allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_xp_gain"),
            new SerializableData()
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifyExperiencePower power = new ModifyExperiencePower(type, player);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("action_on_block_break"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null)
                .add("block_action", SerializableDataType.BLOCK_ACTION, null)
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null)
                .add("only_when_harvested", SerializableDataType.BOOLEAN, true),
            data ->
                (type, player) -> new ActionOnBlockBreakPower(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("block_action"),
                    data.getBoolean("only_when_harvested")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("action_on_land"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> new ActionOnLandPower(type, player,
                    (ActionFactory<Entity>.Instance)data.get("entity_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_entity_render"),
            new SerializableData()
                .add("entity_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new PreventEntityRenderPower(type, player, (ConditionFactory<LivingEntity>.Instance)data.get("entity_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("entity_glow"),
            new SerializableData()
                .add("entity_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new EntityGlowPower(type, player, (ConditionFactory<LivingEntity>.Instance)data.get("entity_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("climbing"),
            new SerializableData()
                .add("allow_holding", SerializableDataType.BOOLEAN, true)
                .add("hold_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> {
                    Predicate<LivingEntity> holdCondition = (ConditionFactory<LivingEntity>.Instance)data.get("hold_condition");
                    return new ClimbingPower(type, player, data.getBoolean("allow_holding"), holdCondition);
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_block_selection"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new PreventBlockSelectionPower(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("self_action_on_kill"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataType.INT)
                .add("hud_render", SerializableDataType.HUD_RENDER, HudRender.DONT_RENDER)
                .add("target_condition", SerializableDataType.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new SelfActionOnKillPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<LivingEntity>.Instance)data.get("target_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("recipe"),
            new SerializableData()
                .add("recipe", SerializableDataType.RECIPE),
            data ->
                (type, player) -> {
                    Recipe<CraftingInventory> recipe = (Recipe<CraftingInventory>)data.get("recipe");
                    return new RecipePower(type, player, recipe);
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("ignore_water"),
            new SerializableData(),
            data ->
                (type, player) -> {
                    return new IgnoreWaterPower(type, player);
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("modify_projectile_damage"),
            new SerializableData()
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null)
                .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataType.ATTRIBUTE_MODIFIERS, null)
                .add("target_condition", SerializableDataType.ENTITY_CONDITION, null)
                .add("self_action", SerializableDataType.ENTITY_ACTION, null)
                .add("target_action", SerializableDataType.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyProjectileDamagePower power = new ModifyProjectileDamagePower(type, player,
                        data.isPresent("damage_condition") ? (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition") : dmg -> true,
                        (ConditionFactory<LivingEntity>.Instance)data.get("target_condition"));
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    if(data.isPresent("self_action")) {
                        power.setSelfAction((ActionFactory<LivingEntity>.Instance)data.get("self_action"));
                    }
                    if(data.isPresent("target_action")) {
                        power.setTargetAction((ActionFactory<LivingEntity>.Instance)data.get("target_action"));
                    }
                    return power;
                })
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("action_on_wake_up"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null)
                .add("block_action", SerializableDataType.BLOCK_ACTION, null)
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new ActionOnWakeUp(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("block_action")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_block_use"),
            new SerializableData()
                .add("block_condition", SerializableDataType.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new PreventBlockUsePower(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("prevent_death"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null)
                .add("damage_condition", SerializableDataType.DAMAGE_CONDITION, null),
            data ->
                (type, player) -> new PreventDeathPower(type, player,
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition")))
            .allowCondition());
        register(new PowerFactory<>(Origins.identifier("action_on_item_use"),
            new SerializableData()
                .add("entity_action", SerializableDataType.ENTITY_ACTION, null)
                .add("item_action", SerializableDataType.ITEM_ACTION, null)
                .add("item_condition", SerializableDataType.ITEM_CONDITION, null),
            data ->
                (type, player) -> new ActionOnItemUsePower(type, player,
                    (ConditionFactory<ItemStack>.Instance)data.get("item_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ActionFactory<ItemStack>.Instance)data.get("item_action")))
            .allowCondition());
    }

    private static void register(PowerFactory serializer) {
        Registry.register(ModRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }
}

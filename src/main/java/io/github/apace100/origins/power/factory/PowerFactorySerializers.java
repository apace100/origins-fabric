package io.github.apace100.origins.power.factory;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.AttributePower;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.TogglePower;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.AttributedEntityAttributeModifier;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class PowerFactorySerializers {

    public static void register() {
        register(new PowerFactory<>(Origins.identifier("simple"), new SerializableData(), data -> Power::new).allowCondition());
        register(new PowerFactory<>(Origins.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataType.BOOLEAN, false),
            data ->
                (type, player) ->
                    new TogglePower(type, player, data.getBoolean("active_by_default")))
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
                })
            .allowCondition());

        /*
        register("simple", new SimplePowerFactory.Serializer());
        register("toggle", new TogglePowerFactory.Serializer());
        register("attribute", new AttributePowerFactory.Serializer());
        register("night_vision", new NightVisionPowerFactory.Serializer());
        register("toggle_night_vision", new ToggleNightVisionPowerFactory.Serializer());
        register("entity_group", new EntityGroupPowerFactory.Serializer());
        register("swim_speed", new SwimSpeedPowerFactory.Serializer());
        register("cooldown", new CooldownPowerFactory.Serializer());
        register("prevent_item_use", new PreventItemUsePowerFactory.Serializer());
        register("prevent_sleep", new PreventSleepPowerFactory.Serializer());
        register("invulnerability", new InvulnerabilityPowerFactory.Serializer());
        register("nether_spawn", new NetherSpawnPowerFactory.Serializer());
        register("modify_damage_dealt", new ModifyDamageDealtPowerFactory.Serializer());
        register("modify_damage_taken", new ModifyDamageTakenPowerFactory.Serializer());
        register("particle", new ParticlePowerFactory.Serializer());
        register("water_vulnerability", new WaterVulnerabilityPowerFactory.Serializer());
        register("effect_immunity", new EffectImmunityPowerFactory.Serializer());
        register("model_color", new ModelColorPowerFactory.Serializer());
        register("phasing", new PhasingPowerFactory.Serializer());
        register("invisibility", new InvisibilityPowerFactory.Serializer());
        register("burn", new BurnPowerFactory.Serializer());
        register("exhaust", new ExhaustOverTimePowerFactory.Serializer());
        register("modify_jump", new ModifyJumpPowerFactory.Serializer());
        register("modify_break_speed", new ModifyBreakSpeedPowerFactory.Serializer());
        register("elytra_flight", new ElytraFlightPowerFactory.Serializer());
        register("restrict_armor", new RestrictArmorPowerFactory.Serializer());
        register("stacking_status_effect", new StackingStatusEffectPowerFactory.Serializer());
        register("fire_projectile", new FireProjectilePowerFactory.Serializer());
        register("modify_exhaustion", new ModifyExhaustionPowerFactory.Serializer());
        register("inventory", new InventoryPowerFactory.Serializer());
        register("modify_harvest", new ModifyHarvestPowerFactory.Serializer());
        register("launch", new LaunchPowerFactory.Serializer());*/
    }
/*
    private static void register(String path, PowerFactory.Serializer serializer) {
        Registry.register(ModRegistries.POWER_FACTORY_SERIALIZER, new Identifier(Origins.MODID, path), serializer);
    }*/
    private static void register(PowerFactory serializer) {
        Registry.register(ModRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }
}

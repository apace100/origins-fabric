package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.*;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.List;
import java.util.function.Predicate;

public class PlayerConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, player) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.PLAYER_CONDITIONS),
            (data, player) -> ((List<ConditionFactory<PlayerEntity>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(player)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.PLAYER_CONDITIONS),
            (data, player) -> ((List<ConditionFactory<PlayerEntity>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(player)
            )));
        register(new ConditionFactory<>(Origins.identifier("block_collision"), new SerializableData()
            .add("offset_x", SerializableDataType.FLOAT)
            .add("offset_y", SerializableDataType.FLOAT)
            .add("offset_z", SerializableDataType.FLOAT),
            (data, player) -> player.world.getBlockCollisions(player,
                player.getBoundingBox().offset(
                    data.getFloat("offset_x") * player.getBoundingBox().getXLength(),
                    data.getFloat("offset_y") * player.getBoundingBox().getYLength(),
                    data.getFloat("offset_z") * player.getBoundingBox().getZLength())
            ).findAny().isPresent()));
        register(new ConditionFactory<>(Origins.identifier("brightness"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getBrightnessAtEyes(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("daytime"), new SerializableData(), (data, player) -> player.world.getTimeOfDay() % 24000L < 13000L));
        register(new ConditionFactory<>(Origins.identifier("time_of_day"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT), (data, player) ->
            ((Comparison)data.get("comparison")).compare(player.world.getTimeOfDay() % 24000L, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("fall_flying"), new SerializableData(), (data, player) -> player.isFallFlying()));
        register(new ConditionFactory<>(Origins.identifier("exposed_to_sun"), new SerializableData(), (data, player) -> {
            if (player.world.isDay() && !player.isBeingRainedOn()) {
                float f = player.getBrightnessAtEyes();
                BlockPos blockPos = player.getVehicle() instanceof BoatEntity ? (new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ())).up() : new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ());
                return f > 0.5F && player.world.isSkyVisible(blockPos);
            }
            return false;
        }));
        register(new ConditionFactory<>(Origins.identifier("in_rain"), new SerializableData(), (data, player) -> player.isBeingRainedOn()));
        register(new ConditionFactory<>(Origins.identifier("invisible"), new SerializableData(), (data, player) -> player.isInvisible()));
        register(new ConditionFactory<>(Origins.identifier("on_fire"), new SerializableData(), (data, player) -> player.isOnFire()));
        register(new ConditionFactory<>(Origins.identifier("exposed_to_sky"), new SerializableData(), (data, player) -> {
            BlockPos blockPos = player.getVehicle() instanceof BoatEntity ? (new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ())).up() : new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ());
            return player.world.isSkyVisible(blockPos);
        }));
        register(new ConditionFactory<>(Origins.identifier("sneaking"), new SerializableData(), (data, player) -> player.isSneaking()));
        register(new ConditionFactory<>(Origins.identifier("sprinting"), new SerializableData(), (data, player) -> player.isSprinting()));
        register(new ConditionFactory<>(Origins.identifier("power_active"), new SerializableData().add("power", SerializableDataType.POWER_TYPE),
            (data, player) -> ((PowerTypeReference<?>)data.get("power")).isActive(player)));
        register(new ConditionFactory<>(Origins.identifier("status_effect"), new SerializableData()
            .add("effect", SerializableDataType.STATUS_EFFECT)
            .add("min_amplifier", SerializableDataType.INT, 0)
            .add("max_amplifier", SerializableDataType.INT, Integer.MAX_VALUE)
            .add("min_duration", SerializableDataType.INT, 0)
            .add("max_duration", SerializableDataType.INT, Integer.MAX_VALUE),
            (data, player) -> {
                StatusEffect effect = data.get("effect");
                if(effect == null) {
                    return false;
                }
                if(player.hasStatusEffect(effect)) {
                    StatusEffectInstance instance = player.getStatusEffect(effect);
                    return instance.getDuration() <= data.getInt("max_duration") && instance.getDuration() >= data.getInt("min_duration")
                        && instance.getAmplifier() <= data.getInt("max_amplifier") && instance.getAmplifier() >= data.getInt("min_amplifier");
                }
                return false;
            }));
        register(new ConditionFactory<>(Origins.identifier("submerged_in"), new SerializableData().add("fluid", SerializableDataType.FLUID_TAG),
            (data, player) -> player.isSubmergedIn(data.get("fluid"))));
        register(new ConditionFactory<>(Origins.identifier("fluid_height"), new SerializableData()
            .add("fluid", SerializableDataType.FLUID_TAG)
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.DOUBLE),
            (data, player) -> data.<Comparison>get("comparison").compare(player.getFluidHeight(data.get("fluid")), data.getDouble("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("origin"), new SerializableData()
            .add("origin", SerializableDataType.IDENTIFIER)
            .add("layer", SerializableDataType.IDENTIFIER, null),
            (data, player) -> {
                OriginComponent component = ModComponents.getOriginComponent(player);
                Identifier originId = data.get("origin");
                if(data.isPresent("layer")) {
                    Identifier layerId = data.get("layer");
                    OriginLayer layer = OriginLayers.getLayer(layerId);
                    if(layer == null) {
                        return false;
                    } else {
                        Origin origin = component.getOrigin(layer);
                        if(origin != null) {
                            return origin.getIdentifier().equals(originId);
                        }
                        return false;
                    }
                } else {
                    return component.getOrigins().values().stream().anyMatch(o -> o.getIdentifier().equals(originId));
                }
            }));
        register(new ConditionFactory<>(Origins.identifier("power"), new SerializableData()
            .add("power", SerializableDataType.IDENTIFIER),
            (data, player) -> {
                try {
                    PowerType<?> powerType = PowerTypeRegistry.get(data.get("power"));
                    return ModComponents.getOriginComponent(player).hasPower(powerType);
                } catch(IllegalArgumentException e) {
                    return false;
                }
            }));
        register(new ConditionFactory<>(Origins.identifier("food_level"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getHungerManager().getFoodLevel(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("saturation_level"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getHungerManager().getSaturationLevel(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("on_block"), new SerializableData()
            .add("block_condition", SerializableDataType.BLOCK_CONDITION, null),
            (data, player) -> player.isOnGround() &&
                (!data.isPresent("block_condition") || ((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
            new CachedBlockPosition(player.world, player.getBlockPos().down(), true)))));
        register(new ConditionFactory<>(Origins.identifier("equipped_item"), new SerializableData()
            .add("equipment_slot", SerializableDataType.EQUIPMENT_SLOT)
            .add("item_condition", SerializableDataType.ITEM_CONDITION),
            (data, player) -> ((ConditionFactory<ItemStack>.Instance)data.get("item_condition")).test(
                player.getEquippedStack(data.get("equipment_slot")))));
        register(new ConditionFactory<>(Origins.identifier("attribute"), new SerializableData()
            .add("attribute", SerializableDataType.ATTRIBUTE)
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.DOUBLE),
            (data, player) -> {
                double attrValue = 0F;
                EntityAttributeInstance attributeInstance = player.getAttributeInstance(data.get("attribute"));
                if(attributeInstance != null) {
                    attrValue = attributeInstance.getValue();
                }
                return ((Comparison)data.get("comparison")).compare(attrValue, data.getDouble("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("swimming"), new SerializableData(), (data, player) -> player.isSwimming()));
        register(new ConditionFactory<>(Origins.identifier("resource"), new SerializableData()
            .add("resource", SerializableDataType.POWER_TYPE)
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, player) -> {
                int resourceValue = 0;
                OriginComponent component = ModComponents.getOriginComponent(player);
                Power p = component.getPower((PowerType<?>)data.get("resource"));
                if(p instanceof VariableIntPower) {
                    resourceValue = ((VariableIntPower)p).getValue();
                }
                return ((Comparison)data.get("comparison")).compare(resourceValue, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("air"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getAir(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("in_block"), new SerializableData()
            .add("block_condition", SerializableDataType.BLOCK_CONDITION),
            (data, player) ->((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
                new CachedBlockPosition(player.world, player.getBlockPos(), true))));
        register(new ConditionFactory<>(Origins.identifier("block_in_radius"), new SerializableData()
            .add("block_condition", SerializableDataType.BLOCK_CONDITION)
            .add("radius", SerializableDataType.INT)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("compare_to", SerializableDataType.INT, 1)
            .add("comparison", SerializableDataType.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
            (data, player) -> {
                Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                for(BlockPos pos : Shape.getPositions(player.getBlockPos(), data.get("shape"), data.getInt("radius"))) {
                    if(blockCondition.test(new CachedBlockPosition(player.world, pos, true))) {
                        count++;
                        if(count == stopAt) {
                            break;
                        }
                    }
                }
                return comparison.compare(count, compareTo);
            }));
        register(new ConditionFactory<>(Origins.identifier("dimension"), new SerializableData()
            .add("dimension", SerializableDataType.IDENTIFIER),
            (data, player) -> player.world.getRegistryKey() == RegistryKey.of(Registry.DIMENSION, data.get("dimension"))));
        register(new ConditionFactory<>(Origins.identifier("xp_levels"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.experienceLevel, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("xp_points"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.totalExperience, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("health"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getHealth(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("relative_health"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getHealth() / player.getMaxHealth(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("biome"), new SerializableData()
            .add("biome", SerializableDataType.IDENTIFIER),
            (data, player) -> player.world.getRegistryManager().get(Registry.BIOME_KEY).getId(player.world.getBiome(player.getBlockPos())).equals(data.get("biome"))));
    }

    private static void register(ConditionFactory<PlayerEntity> conditionFactory) {
        ModRegistries.PLAYER_CONDITION.register(conditionFactory.getSerializerId(), () -> conditionFactory);
    }
}

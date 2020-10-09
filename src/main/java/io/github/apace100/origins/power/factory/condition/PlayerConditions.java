package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerTypeReference;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import io.github.apace100.origins.util.WorldUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class PlayerConditions {

    public static void register() {
        register(new Condition<>(Origins.identifier("block_collision"), new SerializableData()
            .add("offset_x", SerializableDataType.FLOAT)
            .add("offset_y", SerializableDataType.FLOAT)
            .add("offset_z", SerializableDataType.FLOAT),
            (data, player) -> player.world.getBlockCollisions(player,
                player.getBoundingBox().offset(
                    data.getFloat("offset_x") * player.getBoundingBox().getXLength(),
                    data.getFloat("offset_y") * player.getBoundingBox().getYLength(),
                    data.getFloat("offset_z") * player.getBoundingBox().getZLength())
            ).findAny().isPresent()));
        register(new Condition<>(Origins.identifier("brightness"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, player) -> ((Comparison)data.get("comparison")).compare(player.getBrightnessAtEyes(), data.getFloat("compare_to"))));
        register(new Condition<>(Origins.identifier("is_daytime"), new SerializableData(), (data, player) -> player.world.isDay()));
        register(new Condition<>(Origins.identifier("is_fall_flying"), new SerializableData(), (data, player) -> player.isFallFlying()));
        register(new Condition<>(Origins.identifier("is_in_daylight"), new SerializableData(), (data, player) -> {
            Origins.LOGGER.info("checking is in daylight");
            if (player.world.isDay() && !WorldUtil.isRainingAtPlayerPosition(player)) {
                float f = player.getBrightnessAtEyes();
                BlockPos blockPos = player.getVehicle() instanceof BoatEntity ? (new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ())).up() : new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ());
                if (f > 0.5F && player.world.isSkyVisible(blockPos)) {
                    Origins.LOGGER.info("SUCCESS");
                    return true;
                }
            }
            Origins.LOGGER.info("fail");
            return false;
        }));
        register(new Condition<>(Origins.identifier("is_invisible"), new SerializableData(), (data, player) -> player.isInvisible()));
        register(new Condition<>(Origins.identifier("is_on_fire"), new SerializableData(), (data, player) -> player.isOnFire()));
        register(new Condition<>(Origins.identifier("is_sky_visible"), new SerializableData(), (data, player) -> {
            BlockPos blockPos = player.getVehicle() instanceof BoatEntity ? (new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ())).up() : new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ());
            return player.world.isSkyVisible(blockPos);
        }));
        register(new Condition<>(Origins.identifier("is_sneaking"), new SerializableData(), (data, player) -> player.isSneaking()));
        register(new Condition<>(Origins.identifier("is_sprinting"), new SerializableData(), (data, player) -> player.isSprinting()));
        register(new Condition<>(Origins.identifier("power_active"), new SerializableData().add("power", SerializableDataType.POWER_TYPE),
            (data, player) -> ((PowerTypeReference)data.get("power")).isActive(player)));
        register(new Condition<>(Origins.identifier("status_effect"), new SerializableData()
            .add("effect", SerializableDataType.STATUS_EFFECT)
            .add("min_amplifier", SerializableDataType.INT)
            .add("max_amplifier", SerializableDataType.INT)
            .add("min_duration", SerializableDataType.INT)
            .add("max_duration", SerializableDataType.INT),
            (data, player) -> {
                StatusEffect effect = (StatusEffect)data.get("effect");
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
        register(new Condition<>(Origins.identifier("submerged_in"), new SerializableData().add("fluid", SerializableDataType.FLUID_TAG),
            (data, player) -> player.isSubmergedIn((Tag<Fluid>)data.get("fluid"))));
    }

    private static void register(Condition<PlayerEntity> condition) {
        Registry.register(ModRegistries.PLAYER_CONDITION, condition.getSerializerId(), condition);
    }
}

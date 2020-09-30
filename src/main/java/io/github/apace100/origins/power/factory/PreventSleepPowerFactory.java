package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PreventSleepPower;
import io.github.apace100.origins.power.factory.condition.block.BlockCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class PreventSleepPowerFactory extends PowerFactory<PreventSleepPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "prevent_sleep");

    private final List<List<BlockCondition>> blockConditions;
    private final String message;

    public PreventSleepPowerFactory(List<List<BlockCondition>> blockConditions, String message) {
        this.blockConditions = blockConditions;
        this.message = message;
    }

    @Override
    public PreventSleepPower apply(PowerType<PreventSleepPower> powerType, PlayerEntity playerEntity) {
        PreventSleepPower power = new PreventSleepPower(powerType, playerEntity, SerializationHelper.buildBlockConditionPredicate(blockConditions), message);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<PreventSleepPowerFactory> {

        @Override
        public void write(PreventSleepPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeBlockConditions(factory.blockConditions, buf);
            buf.writeString(factory.message);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public PreventSleepPowerFactory read(PacketByteBuf buf) {
            List<List<BlockCondition>> conditions = SerializationHelper.readBlockConditions(buf);
            String message = buf.readString();
            PreventSleepPowerFactory factory = new PreventSleepPowerFactory(conditions, message);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public PreventSleepPowerFactory read(JsonObject json) {
            List<List<BlockCondition>> conditions = null;
            if(json.has("bed_condition")) {
                conditions = SerializationHelper.readBlockConditions(json.get("bed_condition"));
            }
            String message = JsonHelper.getString(json, "message", "origins.cant_sleep");
            PreventSleepPowerFactory factory = new PreventSleepPowerFactory(conditions, message);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModifyHarvestPower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.block.BlockCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class ModifyHarvestPowerFactory extends PowerFactory<ModifyHarvestPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "modify_harvest");

    private final List<List<BlockCondition>> blockConditions;
    private final boolean allow;

    public ModifyHarvestPowerFactory(List<List<BlockCondition>> blockConditions, boolean allow) {
        this.blockConditions = blockConditions;
        this.allow = allow;
    }

    @Override
    public ModifyHarvestPower apply(PowerType<ModifyHarvestPower> powerType, PlayerEntity playerEntity) {
        ModifyHarvestPower power = new ModifyHarvestPower(powerType, playerEntity, SerializationHelper.buildBlockConditionPredicate(blockConditions), allow);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModifyHarvestPowerFactory> {

        @Override
        public void write(ModifyHarvestPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeBlockConditions(factory.blockConditions, buf);
            buf.writeBoolean(factory.allow);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModifyHarvestPowerFactory read(PacketByteBuf buf) {
            List<List<BlockCondition>> conditions = SerializationHelper.readBlockConditions(buf);
            boolean allow = buf.readBoolean();
            ModifyHarvestPowerFactory factory = new ModifyHarvestPowerFactory(conditions, allow);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModifyHarvestPowerFactory read(JsonObject json) {
            List<List<BlockCondition>> conditions = null;
            if(json.has("block_condition")) {
                conditions = SerializationHelper.readBlockConditions(json.get("block_condition"));
            }
            if(!json.has("allow") || !json.get("allow").isJsonPrimitive()) {
                throw new JsonSyntaxException("ModifyHarvestPower json requires \"allow\" boolean.");
            }
            boolean allow = JsonHelper.getBoolean(json, "allow");
            ModifyHarvestPowerFactory factory = new ModifyHarvestPowerFactory(conditions, allow);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PhasingPower;
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

public class PhasingPowerFactory extends PowerFactory<PhasingPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "phasing");

    private final List<List<BlockCondition>> blockConditions;
    private final boolean isBlacklist;

    private final PhasingPower.RenderType renderType;
    private final float viewDistance;

    public PhasingPowerFactory(List<List<BlockCondition>> blockConditions, boolean isBlacklist, PhasingPower.RenderType renderType, float viewDistance) {
        this.blockConditions = blockConditions;
        this.isBlacklist = isBlacklist;
        this.renderType = renderType;
        this.viewDistance = viewDistance;
    }

    @Override
    public PhasingPower apply(PowerType<PhasingPower> powerType, PlayerEntity playerEntity) {
        PhasingPower power = new PhasingPower(powerType, playerEntity, SerializationHelper.buildBlockConditionPredicate(blockConditions), isBlacklist, renderType, viewDistance);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<PhasingPowerFactory> {

        @Override
        public void write(PhasingPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeBlockConditions(factory.blockConditions, buf);
            buf.writeBoolean(factory.isBlacklist);
            buf.writeByte(factory.renderType.ordinal());
            buf.writeFloat(factory.viewDistance);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public PhasingPowerFactory read(PacketByteBuf buf) {
            List<List<BlockCondition>> conditions = SerializationHelper.readBlockConditions(buf);
            boolean blacklist = buf.readBoolean();
            PhasingPower.RenderType render = PhasingPower.RenderType.values()[buf.readByte()];
            float view = buf.readFloat();
            PhasingPowerFactory factory = new PhasingPowerFactory(conditions, blacklist, render, view);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public PhasingPowerFactory read(JsonObject json) {
            List<List<BlockCondition>> conditions = null;
            if(json.has("block_condition")) {
                conditions = SerializationHelper.readBlockConditions(json.get("block_condition"));
            }
            boolean blacklist = JsonHelper.getBoolean(json, "blacklist", false);
            PhasingPower.RenderType renderType = PhasingPower.RenderType.BLINDNESS;
            if(json.has("render_type")) {
                renderType = PhasingPower.RenderType.valueOf(JsonHelper.getString(json, "render_type", "blindness").toUpperCase());
            }
            float viewDistance = JsonHelper.getFloat(json, "view_distance", renderType == PhasingPower.RenderType.BLINDNESS ? 10F : 1F);
            viewDistance = Math.max(1, viewDistance);
            PhasingPowerFactory factory = new PhasingPowerFactory(conditions, blacklist, renderType, viewDistance);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

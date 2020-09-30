package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.TogglePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TogglePowerFactory extends PowerFactory<TogglePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "toggle");
    private final boolean activeByDefault;

    public TogglePowerFactory(boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
    }

    @Override
    public TogglePower apply(PowerType<TogglePower> powerType, PlayerEntity playerEntity) {
        TogglePower power = new TogglePower(powerType, playerEntity, activeByDefault);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<TogglePowerFactory> {

        @Override
        public void write(TogglePowerFactory factory, PacketByteBuf buf) {
            buf.writeBoolean(factory.activeByDefault);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public TogglePowerFactory read(PacketByteBuf buf) {
            TogglePowerFactory factory = new TogglePowerFactory(buf.readBoolean());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public TogglePowerFactory read(JsonObject json) {
            TogglePowerFactory factory = new TogglePowerFactory(JsonHelper.getBoolean(json, "active_by_default", false));
            super.readConditions(factory, json);
            return factory;
        }
    }
}

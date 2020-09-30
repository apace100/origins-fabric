package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.BurnPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class BurnPowerFactory extends PowerFactory<BurnPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "burn");

    private final int refreshInterval;
    private final int burnDuration;

    public BurnPowerFactory(int refreshInterval, int burnDuration) {
        this.refreshInterval = refreshInterval;
        this.burnDuration = burnDuration;
    }

    @Override
    public BurnPower apply(PowerType<BurnPower> powerType, PlayerEntity playerEntity) {
        BurnPower power = new BurnPower(powerType, playerEntity, refreshInterval, burnDuration);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<BurnPowerFactory> {

        @Override
        public void write(BurnPowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.refreshInterval);
            buf.writeInt(factory.burnDuration);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public BurnPowerFactory read(PacketByteBuf buf) {
            BurnPowerFactory factory = new BurnPowerFactory(buf.readInt(), buf.readInt());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public BurnPowerFactory read(JsonObject json) {
            if(!json.has("interval")) {
                throw new JsonSyntaxException("BurnPower json requires \"interval\" integer.");
            }
            int interval = JsonHelper.getInt(json, "interval");
            if(!json.has("burn_duration")) {
                throw new JsonSyntaxException("BurnPower json requires \"burn_duration\" integer.");
            }
            int burn = JsonHelper.getInt(json, "burn_duration");
            BurnPowerFactory factory = new BurnPowerFactory(interval, burn);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

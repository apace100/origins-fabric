package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.CooldownPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CooldownPowerFactory extends PowerFactory<CooldownPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "cooldown");

    private final int cooldownDuration;
    private final int barIndex;
    private final boolean shouldRender;

    public CooldownPowerFactory(int cooldownDuration, int barIndex, boolean shouldRender) {
        this.cooldownDuration = cooldownDuration;
        this.barIndex = barIndex;
        this.shouldRender = shouldRender;
    }

    @Override
    public CooldownPower apply(PowerType<CooldownPower> powerType, PlayerEntity playerEntity) {
        CooldownPower power = new CooldownPower(powerType, playerEntity, cooldownDuration, barIndex, shouldRender);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<CooldownPowerFactory> {

        @Override
        public void write(CooldownPowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.cooldownDuration);
            buf.writeInt(factory.barIndex);
            buf.writeBoolean(factory.shouldRender);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public CooldownPowerFactory read(PacketByteBuf buf) {
            int cd = buf.readInt();
            int bi = buf.readInt();
            boolean sr = buf.readBoolean();
            CooldownPowerFactory factory = new CooldownPowerFactory(cd, bi, sr);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public CooldownPowerFactory read(JsonObject json) {
            if(!json.has("cooldown")) {
                throw new JsonSyntaxException("CooldownPower json requires \"cooldown\" integer.");
            }
            int cooldown = JsonHelper.getInt(json, "cooldown");
            int barIndex = 0;
            boolean shouldRender = false;
            if(json.has("bar_index")) {
                barIndex = JsonHelper.getInt(json, "bar_index", 0);
                shouldRender = JsonHelper.getBoolean(json, "renders", true);
            }
            CooldownPowerFactory factory = new CooldownPowerFactory(cooldown, barIndex, shouldRender);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

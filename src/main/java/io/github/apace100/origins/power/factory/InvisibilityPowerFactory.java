package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.InvisibilityPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class InvisibilityPowerFactory extends PowerFactory<InvisibilityPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "invisibility");
    private final boolean renderArmor;

    public InvisibilityPowerFactory(boolean renderArmor) {
        this.renderArmor = renderArmor;
    }

    @Override
    public InvisibilityPower apply(PowerType<InvisibilityPower> powerType, PlayerEntity playerEntity) {
        InvisibilityPower power = new InvisibilityPower(powerType, playerEntity, renderArmor);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<InvisibilityPowerFactory> {

        @Override
        public void write(InvisibilityPowerFactory factory, PacketByteBuf buf) {
            buf.writeBoolean(factory.renderArmor);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public InvisibilityPowerFactory read(PacketByteBuf buf) {
            InvisibilityPowerFactory factory = new InvisibilityPowerFactory(buf.readBoolean());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public InvisibilityPowerFactory read(JsonObject json) {
            InvisibilityPowerFactory factory = new InvisibilityPowerFactory(JsonHelper.getBoolean(json, "render_armor", true));
            super.readConditions(factory, json);
            return factory;
        }
    }
}

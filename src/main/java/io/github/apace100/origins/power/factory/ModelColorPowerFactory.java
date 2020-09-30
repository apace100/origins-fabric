package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModelColorPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ModelColorPowerFactory extends PowerFactory<ModelColorPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "model_color");

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public ModelColorPowerFactory(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public ModelColorPower apply(PowerType<ModelColorPower> powerType, PlayerEntity playerEntity) {
        ModelColorPower power = new ModelColorPower(powerType, playerEntity, red, green, blue, alpha);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModelColorPowerFactory> {

        @Override
        public void write(ModelColorPowerFactory factory, PacketByteBuf buf) {
            buf.writeFloat(factory.red);
            buf.writeFloat(factory.green);
            buf.writeFloat(factory.blue);
            buf.writeFloat(factory.alpha);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModelColorPowerFactory read(PacketByteBuf buf) {
            float red = buf.readFloat();
            float green = buf.readFloat();
            float blue = buf.readFloat();
            float alpha = buf.readFloat();
            ModelColorPowerFactory factory = new ModelColorPowerFactory(red, green, blue, alpha);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModelColorPowerFactory read(JsonObject json) {
            float red = JsonHelper.getFloat(json, "red", 1.0F);
            float green = JsonHelper.getFloat(json, "green", 1.0F);
            float blue = JsonHelper.getFloat(json, "blue", 1.0F);
            float alpha = JsonHelper.getFloat(json, "alpha", 1.0F);
            ModelColorPowerFactory factory = new ModelColorPowerFactory(red, green, blue, alpha);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

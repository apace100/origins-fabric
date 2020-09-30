package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.InventoryPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class InventoryPowerFactory extends PowerFactory<InventoryPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "inventory");

    private final int rows;
    private final String containerName;

    public InventoryPowerFactory(int rows, String containerName) {
        this.rows = rows;
        this.containerName = containerName;
    }


    @Override
    public InventoryPower apply(PowerType<InventoryPower> powerType, PlayerEntity playerEntity) {
        InventoryPower power = new InventoryPower(powerType, playerEntity, containerName, 9);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<InventoryPowerFactory> {

        @Override
        public void write(InventoryPowerFactory factory, PacketByteBuf buf) {
            buf.writeString(factory.containerName);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public InventoryPowerFactory read(PacketByteBuf buf) {
            InventoryPowerFactory factory = new InventoryPowerFactory(0, buf.readString());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public InventoryPowerFactory read(JsonObject json) {
            String containerName = JsonHelper.getString(json, "name", "container.inventory");
            InventoryPowerFactory factory = new InventoryPowerFactory(0, containerName);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

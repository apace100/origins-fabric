package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerTypeReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class PowerActiveCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "power_active");

    private final PowerTypeReference power;

    public PowerActiveCondition(String powerId) {
        this(new PowerTypeReference(Identifier.tryParse(powerId)));
    }

    public PowerActiveCondition(PowerTypeReference power) {
        this.power = power;
    }

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        if(this.power == null) {
            return false;
        }
        return power.isActive(playerEntity);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<PowerActiveCondition> {

        @Override
        public void write(PowerActiveCondition condition, PacketByteBuf buf) {
            buf.writeIdentifier(condition.power.getIdentifier());
        }

        @Override
        public PowerActiveCondition read(PacketByteBuf buf) {
            Identifier id = buf.readIdentifier();
            return new PowerActiveCondition(new PowerTypeReference(id));
        }

        @Override
        public PowerActiveCondition read(JsonObject json) {
            String powerId = JsonHelper.getString(json, "power", "");
            if(powerId.isEmpty()) {
                throw new JsonSyntaxException("PowerActiveCondition json requires \"power\" identifier.");
            }
            return new PowerActiveCondition(powerId);
        }
    }
}

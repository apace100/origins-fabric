package io.github.apace100.origins.power.factory.condition.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

public class NameCondition extends DamageCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "name");

    private final String damageSourceType;

    public NameCondition(String damageSourceType) {
        this.damageSourceType = damageSourceType;
    }

    @Override
    protected boolean isFulfilled(Pair<DamageSource, Float> damage) {
        return damage.getLeft().getName().equals(damageSourceType);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends DamageCondition.Serializer<NameCondition> {

        @Override
        public void write(NameCondition condition, PacketByteBuf buf) {
            buf.writeString(condition.damageSourceType);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public NameCondition read(PacketByteBuf buf) {
            return new NameCondition(buf.readString());
        }

        @Override
        public NameCondition read(JsonObject json) {
            String type = JsonHelper.getString(json, "name", "");
            if(type.isEmpty()) {
                throw new JsonSyntaxException("NameCondition json requires \"name\" string");
            }
            return new NameCondition(type);
        }
    }
}

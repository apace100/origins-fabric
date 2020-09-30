package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.Comparison;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class BrightnessCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "brightness");

    private final Comparison comparison;
    private final float compareTo;

    public BrightnessCondition(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        return comparison.compare(playerEntity.getBrightnessAtEyes(), compareTo);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<BrightnessCondition> {

        @Override
        public void write(BrightnessCondition condition, PacketByteBuf buf) {
            buf.writeInt(condition.comparison.ordinal());
            buf.writeFloat(condition.compareTo);
        }

        @Override
        public BrightnessCondition read(PacketByteBuf buf) {
            Comparison comparison = Comparison.values()[buf.readInt()];
            float compareTo = buf.readFloat();
            return new BrightnessCondition(comparison, compareTo);
        }

        @Override
        public BrightnessCondition read(JsonObject json) {
            String comparisonString = JsonHelper.getString(json, "comparison", "");
            Comparison comparison = Comparison.getFromString(comparisonString);
            if(comparison == Comparison.NONE) {
                throw new JsonSyntaxException("BrightnessCondition json requires \"comparison\" string (==, <, >, <=, or >=)");
            }
            if(!json.has("compare_to")) {
                throw new JsonSyntaxException("BrightnessCondition json requires \"compare_to\" float");
            }
            float compareTo = JsonHelper.getFloat(json, "compare_to");
            return new BrightnessCondition(comparison, compareTo);
        }
    }
}

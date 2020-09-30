package io.github.apace100.origins.power.factory.condition.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.Comparison;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class HeightCondition extends BlockCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "height");

    private final Comparison comparison;
    private final int compareTo;

    public HeightCondition(Comparison comparison, int compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    protected boolean isFulfilled(CachedBlockPosition cachedBlockPosition) {
        return comparison.compare(cachedBlockPosition.getBlockPos().getY(), compareTo);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends BlockCondition.Serializer<HeightCondition> {

        @Override
        public void write(HeightCondition condition, PacketByteBuf buf) {
            buf.writeInt(condition.comparison.ordinal());
            buf.writeInt(condition.compareTo);
        }

        @Override
        public HeightCondition read(PacketByteBuf buf) {
            Comparison comparison = Comparison.values()[buf.readInt()];
            int compareTo = buf.readInt();
            return new HeightCondition(comparison, compareTo);
        }

        @Override
        public HeightCondition read(JsonObject json) {
            String comparisonString = JsonHelper.getString(json, "comparison", "");
            Comparison comparison = Comparison.getFromString(comparisonString);
            if(comparison == Comparison.NONE) {
                throw new JsonSyntaxException("HeightCondition json requires \"comparison\" string (==, <, >, <=, or >=)");
            }
            if(!json.has("compare_to")) {
                throw new JsonSyntaxException("HeightCondition json requires \"compare_to\" integer");
            }
            int compareTo = JsonHelper.getInt(json, "compare_to");
            return new HeightCondition(comparison, compareTo);
        }
    }
}

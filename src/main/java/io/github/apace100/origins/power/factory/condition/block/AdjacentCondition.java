package io.github.apace100.origins.power.factory.condition.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.Comparison;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;

public class AdjacentCondition extends BlockCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "adjacent");

    private final BlockCondition adjacentCondition;
    private final Comparison comparison;
    private final int compareTo;

    public AdjacentCondition(BlockCondition adjacentCondition, Comparison comparison, int compareTo) {
        this.adjacentCondition = adjacentCondition;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    protected boolean isFulfilled(CachedBlockPosition cachedBlockPosition) {
        int adjacent = 0;
        for(Direction d : Direction.values()) {
            if(adjacentCondition.isFulfilled(new CachedBlockPosition(cachedBlockPosition.getWorld(), cachedBlockPosition.getBlockPos().offset(d), false))) {
                adjacent++;
            }
        }
        return comparison.compare(adjacent, compareTo);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends BlockCondition.Serializer<AdjacentCondition> {

        @Override
        public void write(AdjacentCondition condition, PacketByteBuf buf) {
            BlockCondition.write(condition.adjacentCondition, buf);
            buf.writeInt(condition.comparison.ordinal());
            buf.writeInt(condition.compareTo);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public AdjacentCondition read(PacketByteBuf buf) {
            BlockCondition adjacentCondition = BlockCondition.read(buf);
            Comparison comparison = Comparison.values()[buf.readInt()];
            int compareTo = buf.readInt();
            return new AdjacentCondition(adjacentCondition, comparison, compareTo);
        }

        @Override
        public AdjacentCondition read(JsonObject json) {
            BlockCondition condition;
            if(!json.has("adjacent_condition")) {
                condition = new IsBlockCondition(new Identifier("minecraft:air"));
                condition.isInverted = true;
            } else {
                condition = BlockCondition.read(json.get("adjacent_condition"));
            }
            String comparisonString = JsonHelper.getString(json, "comparison", "");
            Comparison comparison = Comparison.getFromString(comparisonString);
            if(comparison == Comparison.NONE) {
                throw new JsonSyntaxException("AdjacentCondition json requires \"comparison\" string (==, <, >, <=, or >=)");
            }
            if(!json.has("compare_to")) {
                throw new JsonSyntaxException("AdjacentCondition json requires \"compare_to\" integer");
            }
            int compareTo = JsonHelper.getInt(json, "compare_to");
            return new AdjacentCondition(condition, comparison, compareTo);
        }
    }
}

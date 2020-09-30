package io.github.apace100.origins.power.factory.condition.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.Comparison;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

public class AmountCondition extends DamageCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "amount");

    private final Comparison comparison;
    private final float compareTo;

    public AmountCondition(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    protected boolean isFulfilled(Pair<DamageSource, Float> damage) {
        return damage.getRight() == null || comparison.compare(damage.getRight(), compareTo);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends DamageCondition.Serializer<AmountCondition> {

        @Override
        public void write(AmountCondition condition, PacketByteBuf buf) {
            buf.writeInt(condition.comparison.ordinal());
            buf.writeFloat(condition.compareTo);
        }

        @Override
        public AmountCondition read(PacketByteBuf buf) {
            Comparison comparison = Comparison.values()[buf.readInt()];
            float compareTo = buf.readFloat();
            return new AmountCondition(comparison, compareTo);
        }

        @Override
        public AmountCondition read(JsonObject json) {
            String comparisonString = JsonHelper.getString(json, "comparison", "");
            Comparison comparison = Comparison.getFromString(comparisonString);
            if(comparison == Comparison.NONE) {
                throw new JsonSyntaxException("AmountCondition json requires \"comparison\" string (==, <, >, <=, or >=)");
            }
            if(!json.has("compare_to")) {
                throw new JsonSyntaxException("AmountCondition json requires \"compare_to\" float");
            }
            float compareTo = JsonHelper.getFloat(json, "compare_to");
            return new AmountCondition(comparison, compareTo);
        }
    }
}

package io.github.apace100.origins.power.factory.condition.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.Comparison;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ArmorValueCondition extends ItemCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "armor_value");

    private final Comparison comparison;
    private final int compareTo;

    public ArmorValueCondition(Comparison comparison, int compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean isFulfilled(ItemStack stack) {
        int armor = 0;
        if(stack.getItem() instanceof ArmorItem) {
            ArmorItem item = (ArmorItem)stack.getItem();
            armor = item.getProtection();
        }
        return comparison.compare(armor, compareTo);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends ItemCondition.Serializer<ArmorValueCondition> {

        @Override
        public void write(ArmorValueCondition condition, PacketByteBuf buf) {
            buf.writeInt(condition.comparison.ordinal());
            buf.writeInt(condition.compareTo);
        }

        @Override
        public ArmorValueCondition read(PacketByteBuf buf) {
            Comparison comparison = Comparison.values()[buf.readInt()];
            int compareTo = buf.readInt();
            return new ArmorValueCondition(comparison, compareTo);
        }

        @Override
        public ArmorValueCondition read(JsonObject json) {
            String comparisonString = JsonHelper.getString(json, "comparison", "");
            Comparison comparison = Comparison.getFromString(comparisonString);
            if(comparison == Comparison.NONE) {
                throw new JsonSyntaxException("ArmorValueCondition json requires \"comparison\" string (==, <, >, <=, or >=)");
            }
            if(!json.has("compare_to")) {
                throw new JsonSyntaxException("ArmorValueCondition json requires \"compare_to\" integer");
            }
            int compareTo = JsonHelper.getInt(json, "compare_to");
            return new ArmorValueCondition(comparison, compareTo);
        }
    }
}

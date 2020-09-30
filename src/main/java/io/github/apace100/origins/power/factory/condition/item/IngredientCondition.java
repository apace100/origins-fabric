package io.github.apace100.origins.power.factory.condition.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public class IngredientCondition extends ItemCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "ingredient");

    private final Ingredient ingredient;

    public IngredientCondition(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean isFulfilled(ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends ItemCondition.Serializer<IngredientCondition> {

        @Override
        public void write(IngredientCondition condition, PacketByteBuf buf) {
            condition.ingredient.write(buf);
        }

        @Override
        public IngredientCondition read(PacketByteBuf buf) {
            Ingredient ingr = Ingredient.fromPacket(buf);
            return new IngredientCondition(ingr);
        }

        @Override
        public IngredientCondition read(JsonObject json) {
            if(!json.has("ingredient") || json.get("ingredient").isJsonPrimitive()) {
                throw new JsonSyntaxException("IngredientCondition json requires \"ingredient\" object or array");
            }
            Ingredient ingr = Ingredient.fromJson(json.get("ingredient"));
            return new IngredientCondition(ingr);
        }
    }
}

package io.github.apace100.origins.screen.badge;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.power.RecipePower;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class CraftingRecipeBadge extends TooltipBadge {
    private final Recipe<CraftingInventory> recipe;
    private final DefaultedList<ItemStack[]> inputs = DefaultedList.ofSize(9, new ItemStack[0]);
    private final ItemStack output;

    public CraftingRecipeBadge(Identifier spriteLocation, String rawText, Recipe<CraftingInventory> recipe) {
        super(spriteLocation, rawText);
        this.recipe = recipe;
        DefaultedList<Ingredient> ingredients = recipe.getIngredients();
        for(int i = 0; i < ingredients.size(); ++i) {
            Ingredient ingredient = ingredients.get(i);
            if(!ingredient.isEmpty()) {
                this.inputs.set(i, ingredient.getMatchingStacks());
            }
        }
        this.output = recipe.getOutput();
    }

    private DefaultedList<ItemStack> peekInputs(float time) {
        int seed = MathHelper.floor(time / 30);
        DefaultedList<ItemStack> inputs = DefaultedList.ofSize(9, ItemStack.EMPTY);
        for(int index = 0; index < 9; ++index) {
            ItemStack[] stacks = this.inputs.get(index);
            if(stacks.length > 0) inputs.set(index, stacks[seed % stacks.length]);
        }
        return inputs;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<TooltipComponent> getTooltipComponent(TextRenderer textRenderer, int widthLimit, float time) {
        widthLimit = 130;
        List<TooltipComponent> tooltipComponents = super.getTooltipComponent(textRenderer, widthLimit, time);
        tooltipComponents.add(new CraftingRecipeTooltipComponent(this.peekInputs(time), this.output));
        return tooltipComponents;
    }

    public static BadgeFactory<CraftingRecipeBadge> craftingRecipeBadgeFactory() {
        SerializableData data = new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("text", SerializableDataTypes.STRING, "")
            .add("recipe", SerializableDataTypes.RECIPE, null);
        return new BadgeFactory<>(
            Origins.identifier("crafting_recipe"),
            data,
            (powerType, jsonObject) -> {
                if(powerType.create(null) instanceof RecipePower rp) {
                    SerializableData.Instance instance = data.read(jsonObject);
                    instance.set("recipe", rp.getRecipe());
                    return instance;
                }
                throw new JsonSyntaxException("Trying to create CraftingRecipeBadge but found non RecipePower " + powerType.getIdentifier() + " instead!");
            },
            instance -> new CraftingRecipeBadge(
                instance.getId("sprite"),
                instance.getString("text"),
                instance.get("recipe")
            )
        );
    }

}

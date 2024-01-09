package io.github.apace100.origins.badge;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.screen.tooltip.CraftingRecipeTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public record CraftingRecipeBadge(Identifier spriteId,
                                  RecipeEntry<Recipe<CraftingInventory>> recipe,
                                  @Nullable Text prefix,
                                  @Nullable Text suffix) implements Badge {

    public CraftingRecipeBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"),
            instance.get("recipe"),
            instance.get("prefix"),
            instance.get("suffix"));
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    public DefaultedList<ItemStack> peekInputs(float time) {

        DefaultedList<ItemStack> inputs = DefaultedList.ofSize(9, ItemStack.EMPTY);
        List<Ingredient> ingredients = this.recipe.value().getIngredients();

        int seed = MathHelper.floor(time / 30);
        for (int index = 0; index < ingredients.size(); index++) {

            ItemStack[] stacks = ingredients.get(index).getMatchingStacks();
            if (stacks.length > 0) {
                inputs.set(index, stacks[seed % stacks.length]);
            }

        }

        return inputs;

    }

    @Environment(EnvType.CLIENT)
    @Override
    public List<TooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, TextRenderer textRenderer) {

        MinecraftClient client = MinecraftClient.getInstance();
        List<TooltipComponent> tooltips = new LinkedList<>();

        if (client.world == null) {
            Origins.LOGGER.warn("Could not construct crafting recipe badge, as world was null!");
            return tooltips;
        }

        int recipeWidth = (Recipe<?>) recipe.value() instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getWidth() : 3;

        DynamicRegistryManager registryManager = client.world.getRegistryManager();
        StackReference outputStackReference = InventoryUtil.createStackReference(recipe.value().getResult(registryManager));

        PowerHolderComponent.getPowers(client.player, ModifyCraftingPower.class)
            .stream()
            .filter(p -> p.doesApply(recipe.id(), outputStackReference.get()))
            .max(Comparator.comparing(ModifyCraftingPower::getPriority))
            .ifPresent(p -> p.getNewResult(outputStackReference));
        CraftingRecipeTooltipComponent recipeTooltip = new CraftingRecipeTooltipComponent(recipeWidth, this.peekInputs(time), outputStackReference.get());

        if (client.options.advancedItemTooltips) {

            Text recipeIdText = Text.literal(recipe.id().toString()).formatted(Formatting.DARK_GRAY);
            widthLimit = Math.max(130, textRenderer.getWidth(recipeIdText));

            if (prefix != null) {
                TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            }

            tooltips.add(recipeTooltip);
            if (suffix != null) {
                TooltipBadge.addLines(tooltips, suffix, textRenderer, widthLimit);
            }

            TooltipBadge.addLines(tooltips, recipeIdText, textRenderer, widthLimit);

        } else {

            widthLimit = 130;
            if (prefix != null) {
                TooltipBadge.addLines(tooltips, prefix, textRenderer, widthLimit);
            }

            tooltips.add(recipeTooltip);
            if (suffix != null) {
                TooltipBadge.addLines(tooltips, suffix, textRenderer, widthLimit);
            }

        }

        return tooltips;

    }

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        instance.set("recipe", recipe);
        instance.set("prefix", prefix);
        instance.set("suffix", suffix);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.CRAFTING_RECIPE;
    }

}

package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;

public class RecipePower extends Power {

    private final Recipe<CraftingInventory> recipe;

    public RecipePower(PowerType<?> type, PlayerEntity player, Recipe<CraftingInventory> recipe) {
        super(type, player);
        this.recipe = recipe;
    }

    public Recipe<CraftingInventory> getRecipe() {
        return recipe;
    }
}

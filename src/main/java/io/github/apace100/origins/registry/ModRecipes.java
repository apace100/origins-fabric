package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.OriginRestrictedCraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.registry.Registry;

public class ModRecipes {

    public static final SpecialRecipeSerializer<OriginRestrictedCraftingRecipe> ORIGIN_RESTRICTED = register("origin_restricted", new SpecialRecipeSerializer<>(OriginRestrictedCraftingRecipe::new));

    public static void register() {

    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registry.RECIPE_SERIALIZER, Origins.identifier(id), serializer);
    }
}

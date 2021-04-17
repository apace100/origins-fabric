package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.OriginRestrictedCraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;

public class ModRecipes {

    public static final SpecialRecipeSerializer<OriginRestrictedCraftingRecipe> ORIGIN_RESTRICTED = register("origin_restricted", new SpecialRecipeSerializer<>(OriginRestrictedCraftingRecipe::new));

    public static void register() {

    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        ModRegistriesArchitectury.RECIPE_SERIALIZERS.registerSupplied(Origins.identifier(id), () -> serializer);
        return serializer;
    }
}

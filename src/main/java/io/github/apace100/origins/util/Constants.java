package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;

public class Constants {

    public static final int[] LIGHT_ARMOR_MAX_PROTECTION = new int[] {1, 4, 5, 2};

    public static final JsonObject TOGGLE_BADGE_JSON = new JsonObject();
    public static final JsonObject ACTIVE_BADGE_JSON = new JsonObject();
    public static final JsonObject SHAPED_RECIPE_BADGE_JSON = new JsonObject();
    public static final JsonObject SHAPLESS_RECIPE_BADGE_JSON = new JsonObject();
    static {
        TOGGLE_BADGE_JSON.addProperty("sprite", Origins.identifier("textures/gui/badge/toggle.png").toString());
        TOGGLE_BADGE_JSON.addProperty("text", "origins.gui.badge.toggle");
        ACTIVE_BADGE_JSON.addProperty("sprite", Origins.identifier("textures/gui/badge/active.png").toString());
        ACTIVE_BADGE_JSON.addProperty("text", "origins.gui.badge.active");
        SHAPED_RECIPE_BADGE_JSON.addProperty("sprite", Origins.identifier("textures/gui/badge/recipe.png").toString());
        SHAPED_RECIPE_BADGE_JSON.addProperty("text", "origins.gui.badge.recipe.crafting.shaped");
        SHAPLESS_RECIPE_BADGE_JSON.addProperty("sprite", Origins.identifier("textures/gui/badge/recipe.png").toString());
        SHAPLESS_RECIPE_BADGE_JSON.addProperty("text", "origins.gui.badge.recipe.crafting.shapeless");
    }

}

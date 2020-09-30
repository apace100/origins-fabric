package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import net.minecraft.util.Identifier;

public class ModPackets {

    public static final Identifier OPEN_ORIGIN_SCREEN = new Identifier(Origins.MODID, "open_origin_screen");
    public static final Identifier CHOOSE_ORIGIN = new Identifier(Origins.MODID, "choose_origin");
    public static final Identifier USE_ACTIVE_POWER = new Identifier(Origins.MODID, "use_active_power");
    public static final Identifier ORIGIN_LIST = new Identifier(Origins.MODID, "origin_list");
    public static final Identifier LAYER_LIST = new Identifier(Origins.MODID, "layer_list");
    public static final Identifier POWER_LIST = new Identifier(Origins.MODID, "power_list");
}

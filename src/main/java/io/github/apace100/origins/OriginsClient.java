package io.github.apace100.origins;

import io.github.apace100.apoli.integration.PowerClearCallback;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModEntities;
import io.github.apace100.origins.screen.ViewOriginScreen;
import io.github.apace100.origins.util.PowerKeyManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OriginsClient implements ClientModInitializer {

    public static KeyBinding primaryActiveKeyBinding;
    public static KeyBinding secondaryActiveKeyBinding;
    public static KeyBinding viewOriginKeyBinding;

    public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TEMPORARY_COBWEB, RenderLayer.getCutout());
        EntityRendererRegistry.register(ModEntities.ENDERIAN_PEARL, FlyingItemEntityRenderer::new);

        ModPacketsS2C.register();

        primaryActiveKeyBinding = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID);
        secondaryActiveKeyBinding = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + Origins.MODID);
        viewOriginKeyBinding = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);

        KeyBindingUtil.ALIASES.addAlias("primary", primaryActiveKeyBinding.getTranslationKey());
        KeyBindingUtil.ALIASES.addAlias("secondary", secondaryActiveKeyBinding.getTranslationKey());

        //  "none" is the default key used when no keybinding reference is specified in powers
        KeyBindingUtil.ALIASES.addAlias("none", primaryActiveKeyBinding.getTranslationKey());

        KeyBindingHelper.registerKeyBinding(primaryActiveKeyBinding);
        KeyBindingHelper.registerKeyBinding(secondaryActiveKeyBinding);
        KeyBindingHelper.registerKeyBinding(viewOriginKeyBinding);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {

            while (viewOriginKeyBinding.wasPressed()) {

                if (!(client.currentScreen instanceof ViewOriginScreen)) {
                    client.setScreen(new ViewOriginScreen());
                }

            }

        });

        PowerClearCallback.EVENT.register(PowerKeyManager::clearCache);

    }
}

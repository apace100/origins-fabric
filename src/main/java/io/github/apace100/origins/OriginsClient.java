package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.power.Active;
import io.github.apace100.origins.power.factory.condition.PlayerConditionsClient;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModEntities;
import io.github.apace100.origins.screen.PowerHudRenderer;
import io.github.apace100.origins.screen.ViewOriginScreen;
import io.github.apace100.origins.util.OriginsConfig;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class OriginsClient implements ClientModInitializer {

    public static KeyBinding usePrimaryActivePowerKeybind;
    public static KeyBinding useSecondaryActivePowerKeybind;
    public static KeyBinding viewCurrentOriginKeybind;

    public static OriginsConfig config;

    public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TEMPORARY_COBWEB, RenderLayer.getCutout());

        EntityRendererRegistry.INSTANCE.register(ModEntities.ENDERIAN_PEARL,
            (dispatcher, context) -> new FlyingItemEntityRenderer(dispatcher, context.getItemRenderer()));

        ModPacketsS2C.register();

        PlayerConditionsClient.register();

        AutoConfig.register(OriginsConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(OriginsConfig.class).getConfig();

        usePrimaryActivePowerKeybind = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID);
        useSecondaryActivePowerKeybind = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + Origins.MODID);
        viewCurrentOriginKeybind = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);
        KeyBindingHelper.registerKeyBinding(usePrimaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(useSecondaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(viewCurrentOriginKeybind);

        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(usePrimaryActivePowerKeybind.wasPressed()) {
                performActivePower(Active.KeyType.PRIMARY);
            }
            while(useSecondaryActivePowerKeybind.wasPressed()) {
                performActivePower(Active.KeyType.SECONDARY);
            }
            while(viewCurrentOriginKeybind.wasPressed()) {
                if(!(MinecraftClient.getInstance().currentScreen instanceof ViewOriginScreen)) {
                    MinecraftClient.getInstance().openScreen(new ViewOriginScreen());
                }
            }
        });
        new PowerHudRenderer().register();
    }

    @Environment(EnvType.CLIENT)
    private void performActivePower(Active.KeyType key) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeInt(key.ordinal());
        ClientPlayNetworking.send(ModPackets.USE_ACTIVE_POWER, buffer);
        OriginComponent component = ModComponents.ORIGIN.get(MinecraftClient.getInstance().player);
        if(component.hasAllOrigins()) {
            component.getPowers().stream().filter(p -> p instanceof Active && ((Active)p).getKey() == key).forEach(p -> ((Active)p).onUse());
        }
    }
}

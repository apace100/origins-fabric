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
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.RenderTypes;
import me.shedaniel.architectury.registry.entity.EntityRenderers;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class OriginsClient {

    public static KeyBinding usePrimaryActivePowerKeybind;
    public static KeyBinding useSecondaryActivePowerKeybind;
    public static KeyBinding viewCurrentOriginKeybind;

    public static OriginsConfig config;

    public static boolean isServerRunningOrigins = false;

    @Environment(EnvType.CLIENT)
    public static void register() {
        RenderTypes.register(RenderLayer.getCutout(), ModBlocks.TEMPORARY_COBWEB);

        EntityRenderers.register(ModEntities.ENDERIAN_PEARL,
            (dispatcher) -> new FlyingItemEntityRenderer<>(dispatcher, MinecraftClient.getInstance().getItemRenderer()));

        ModPacketsS2C.register();

        PlayerConditionsClient.register();
        OriginClientEventHandler.register();

        AutoConfig.register(OriginsConfig.class, OriginsConfig.Serializer::new);
        config = AutoConfig.getConfigHolder(OriginsConfig.class).getConfig();

        usePrimaryActivePowerKeybind = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID);
        useSecondaryActivePowerKeybind = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + Origins.MODID);
        viewCurrentOriginKeybind = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);
        KeyBindings.registerKeyBinding(usePrimaryActivePowerKeybind);
        KeyBindings.registerKeyBinding(useSecondaryActivePowerKeybind);
        KeyBindings.registerKeyBinding(viewCurrentOriginKeybind);

        ClientTickEvent.CLIENT_PRE.register(tick -> {
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
    private static void performActivePower(Active.KeyType key) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeInt(key.ordinal());
        NetworkManager.sendToServer(ModPackets.USE_ACTIVE_POWER, buffer);
        OriginComponent component = ModComponents.getOriginComponent(MinecraftClient.getInstance().player);
        if(component.hasAllOrigins()) {
            component.getPowers().stream().filter(p -> p instanceof Active && ((Active)p).getKey() == key).forEach(p -> ((Active)p).onUse());
        }
    }
}

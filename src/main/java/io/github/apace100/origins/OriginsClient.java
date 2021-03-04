package io.github.apace100.origins;

import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.power.Active;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.factory.condition.EntityConditionsClient;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModEntities;
import io.github.apace100.origins.screen.PowerHudRenderer;
import io.github.apace100.origins.screen.ViewOriginScreen;
import io.github.apace100.origins.util.OriginsConfig;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class OriginsClient implements ClientModInitializer {

    public static KeyBinding usePrimaryActivePowerKeybind;
    public static KeyBinding useSecondaryActivePowerKeybind;
    public static KeyBinding viewCurrentOriginKeybind;

    public static OriginsConfig config;

    public static boolean isServerRunningOrigins = false;

    private static HashMap<String, KeyBinding> idToKeyBindingMap = new HashMap<>();
    private static HashMap<String, Boolean> lastKeyBindingStates = new HashMap<>();
    private static boolean initializedKeyBindingMap = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TEMPORARY_COBWEB, RenderLayer.getCutout());

        EntityRendererRegistry.INSTANCE.register(ModEntities.ENDERIAN_PEARL,
            (dispatcher, context) -> new FlyingItemEntityRenderer(dispatcher, context.getItemRenderer()));

        ModPacketsS2C.register();

        EntityConditionsClient.register();

        AutoConfig.register(OriginsConfig.class, OriginsConfig.Serializer::new);
        config = AutoConfig.getConfigHolder(OriginsConfig.class).getConfig();

        usePrimaryActivePowerKeybind = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID);
        useSecondaryActivePowerKeybind = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + Origins.MODID);
        viewCurrentOriginKeybind = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);

        idToKeyBindingMap.put("key.origins.primary_active", usePrimaryActivePowerKeybind);
        idToKeyBindingMap.put("key.origins.secondary_active", useSecondaryActivePowerKeybind);
        idToKeyBindingMap.put("primary", usePrimaryActivePowerKeybind);
        idToKeyBindingMap.put("secondary", useSecondaryActivePowerKeybind);

        KeyBindingHelper.registerKeyBinding(usePrimaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(useSecondaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(viewCurrentOriginKeybind);

        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            if(tick.player != null) {
                List<Power> powers = ModComponents.ORIGIN.get(tick.player).getPowers();
                List<Power> pressedPowers = new LinkedList<>();
                for(Power power : powers) {
                    if(power instanceof Active) {
                        Active active = (Active)power;
                        Active.Key key = active.getKey();
                        KeyBinding keyBinding = getKeyBinding(key.key);
                        if(keyBinding != null) {
                            if(keyBinding.isPressed() && (key.continuous || !lastKeyBindingStates.getOrDefault(key.key, false))) {
                                pressedPowers.add(power);
                            }
                            lastKeyBindingStates.put(key.key, keyBinding.isPressed());
                        }
                    }
                }
                if(pressedPowers.size() > 0) {
                    performActivePowers(pressedPowers);
                }
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
    private void performActivePowers(List<Power> powers) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeInt(powers.size());
        for(Power power : powers) {
            buffer.writeIdentifier(power.getType().getIdentifier());
            ((Active)power).onUse();
        }
        ClientPlayNetworking.send(ModPackets.USE_ACTIVE_POWERS, buffer);
    }

    @Environment(EnvType.CLIENT)
    private KeyBinding getKeyBinding(String key) {
        if(!idToKeyBindingMap.containsKey(key)) {
            if(!initializedKeyBindingMap) {
                initializedKeyBindingMap = true;
                MinecraftClient client = MinecraftClient.getInstance();
                for(int i = 0; i < client.options.keysAll.length; i++) {
                    idToKeyBindingMap.put(client.options.keysAll[i].getTranslationKey(), client.options.keysAll[i]);
                }
                return getKeyBinding(key);
            }
            return null;
        }
        return idToKeyBindingMap.get(key);
    }
}

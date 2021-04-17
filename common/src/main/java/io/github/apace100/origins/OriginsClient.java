package io.github.apace100.origins;

import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.power.Active;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.factory.condition.EntityConditionsClient;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModComponentsArchitectury;
import io.github.apace100.origins.registry.ModEntities;
import io.github.apace100.origins.screen.PowerHudRenderer;
import io.github.apace100.origins.screen.ViewOriginScreen;
import io.github.apace100.origins.util.OriginsConfigSerializer;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.registry.KeyBindings;
import me.shedaniel.architectury.registry.RenderTypes;
import me.shedaniel.architectury.registry.entity.EntityRenderers;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

public class OriginsClient {

    public static KeyBinding usePrimaryActivePowerKeybind;
    public static KeyBinding useSecondaryActivePowerKeybind;
    public static KeyBinding viewCurrentOriginKeybind;

    public static ClientConfig config;

    public static boolean isServerRunningOrigins = false;

    private static HashMap<String, KeyBinding> idToKeyBindingMap = new HashMap<>();
    private static HashMap<String, Boolean> lastKeyBindingStates = new HashMap<>();
    private static boolean initializedKeyBindingMap = false;

    public static void registerPowerKeybinding(String keyId, KeyBinding keyBinding) {
        idToKeyBindingMap.put(keyId, keyBinding);
    }

    @Environment(EnvType.CLIENT)
    public static void register() {
        EntityRenderers.register(ModEntities.ENDERIAN_PEARL,
            (dispatcher) -> new FlyingItemEntityRenderer<>(dispatcher, MinecraftClient.getInstance().getItemRenderer()));

        ModPacketsS2C.register();

        EntityConditionsClient.register();
        OriginClientEventHandler.register();

        AutoConfig.register(ClientConfig.class, OriginsConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();

        usePrimaryActivePowerKeybind = new KeyBinding("key.origins.primary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID);
        useSecondaryActivePowerKeybind = new KeyBinding("key.origins.secondary_active", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category." + Origins.MODID);
        viewCurrentOriginKeybind = new KeyBinding("key.origins.view_origin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID);

        registerPowerKeybinding("key.origins.primary_active", usePrimaryActivePowerKeybind);
        registerPowerKeybinding("key.origins.secondary_active", useSecondaryActivePowerKeybind);
        registerPowerKeybinding("primary", usePrimaryActivePowerKeybind);
        registerPowerKeybinding("secondary", useSecondaryActivePowerKeybind);

        KeyBindings.registerKeyBinding(usePrimaryActivePowerKeybind);
        KeyBindings.registerKeyBinding(useSecondaryActivePowerKeybind);
        KeyBindings.registerKeyBinding(viewCurrentOriginKeybind);

        ClientTickEvent.CLIENT_PRE.register(tick -> {
            if(tick.player != null) {
                List<Power> powers = ModComponentsArchitectury.getOriginComponent(tick.player).getPowers();
                List<Power> pressedPowers = new LinkedList<>();
                HashMap<String, Boolean> currentKeyBindingStates = new HashMap<>();
                for(Power power : powers) {
                    if(power instanceof Active) {
                        Active active = (Active)power;
                        Active.Key key = active.getKey();
                        KeyBinding keyBinding = getKeyBinding(key.key);
                        if(keyBinding != null) {
                            if(!currentKeyBindingStates.containsKey(key.key)) {
                               currentKeyBindingStates.put(key.key, keyBinding.isPressed());
                            }
                            if(currentKeyBindingStates.get(key.key) && (key.continuous || !lastKeyBindingStates.getOrDefault(key.key, false))) {
                                pressedPowers.add(power);
                            }
                        }
                    }
                }
                lastKeyBindingStates = currentKeyBindingStates;
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

    /**
     * Forge uses a delegate for RenderType assignment, which means
     * it needs to be called after the registries have initialized.
     */
    public static void setup() {
        RenderTypes.register(RenderLayer.getCutout(), ModBlocks.TEMPORARY_COBWEB);
    }

    @Environment(EnvType.CLIENT)
    private static void performActivePowers(List<Power> powers) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeInt(powers.size());
        for(Power power : powers) {
            buffer.writeIdentifier(power.getType().getIdentifier());
            ((Active)power).onUse();
        }
        NetworkManager.sendToServer(ModPackets.USE_ACTIVE_POWERS, buffer);
    }

    @Environment(EnvType.CLIENT)
    private static KeyBinding getKeyBinding(String key) {
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

    @Config(name = Origins.MODID)
    public static class ClientConfig implements ConfigData {

        public int xOffset = 0;
        public int yOffset = 0;

        @ConfigEntry.BoundedDiscrete(max = 1)
        public float phantomizedOverlayStrength = 0.8F;

        @Override
        public void validatePostLoad() {
            if (phantomizedOverlayStrength < 0F) {
                phantomizedOverlayStrength = 0F;
            } else if (phantomizedOverlayStrength > 1F) {
                phantomizedOverlayStrength = 1F;
            }
        }
    }
}

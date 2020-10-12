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
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class OriginsClient implements ClientModInitializer {

    public static KeyBinding useActivePowerKeybind;
    public static KeyBinding viewCurrentOriginKeybind;

    public static OriginsConfig config;

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

        useActivePowerKeybind = FabricKeyBinding.Builder.create(new Identifier(Origins.MODID, "active_power"),
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID).build();
        KeyBindingHelper.registerKeyBinding(useActivePowerKeybind);
        viewCurrentOriginKeybind = FabricKeyBinding.Builder.create(new Identifier(Origins.MODID, "view_origin"),
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category." + Origins.MODID).build();
        KeyBindingHelper.registerKeyBinding(viewCurrentOriginKeybind);
        ClientTickCallback.EVENT.register(client -> {
            while(useActivePowerKeybind.wasPressed()) {
                ClientSidePacketRegistry.INSTANCE.sendToServer(ModPackets.USE_ACTIVE_POWER, new PacketByteBuf(Unpooled.buffer()));
                OriginComponent component = ModComponents.ORIGIN.get(MinecraftClient.getInstance().player);
                if(component.hasAllOrigins()) {
                    component.getPowers().stream().filter(p -> p instanceof Active).forEach(p -> ((Active)p).onUse());
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
}

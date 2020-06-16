package io.github.apace100.origins;

import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.screen.PowerHudRenderer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class OriginsClient implements ClientModInitializer {

    public static KeyBinding keyBinding;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TEMPORARY_COBWEB, RenderLayer.getCutout());
        ModPacketsS2C.register();

        keyBinding = FabricKeyBinding.Builder.create(new Identifier(Origins.MODID, "active_power"),
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category." + Origins.MODID).build();
        KeyBindingHelper.registerKeyBinding(keyBinding);
        ClientTickCallback.EVENT.register(client -> {
            while(keyBinding.wasPressed()) {
                ClientSidePacketRegistry.INSTANCE.sendToServer(ModPackets.USE_ACTIVE_POWER, new PacketByteBuf(Unpooled.buffer()));
            }
        });
        new PowerHudRenderer().register();
    }
}

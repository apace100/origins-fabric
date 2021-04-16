package io.github.apace100.origins;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class OriginsForgeClient {
	public static void initialize() {
		OriginsClient.register();
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> OriginsForgeClient::buildConfigScreen);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(OriginsForgeClient::clientSetup);
		MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggedInEvent event) -> OriginsClient.isServerRunningOrigins = OriginsForge.channel.isRemotePresent(event.getNetworkManager()));
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		OriginsClient.setup();
	}

	private static Screen buildConfigScreen(MinecraftClient minecraftClient, Screen parent) {
		return AutoConfig.getConfigScreen(OriginsClient.ClientConfig.class, parent).get();
	}
}

package io.github.apace100.origins;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class OriginsForgeClient {
	public static void initialize() {
		OriginsClient.register();
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> OriginsForgeClient::buildConfigScreen);
	}

	private static Screen buildConfigScreen(MinecraftClient minecraftClient, Screen parent) {
		return AutoConfig.getConfigScreen(OriginsClient.ClientConfig.class, parent).get();
	}
}

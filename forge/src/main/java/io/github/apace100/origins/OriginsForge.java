package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;

@Mod(Origins.MODID)
public class OriginsForge {

	public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(Origins.identifier("handshake"), () -> Origins.VERSION, NetworkRegistry.acceptMissingOr(Origins.VERSION), NetworkRegistry.acceptMissingOr(Origins.VERSION));

	public OriginsForge() {
		ArtifactVersion version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion();
		Origins.VERSION = version.toString();
		Origins.SEMVER = new int[] {version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion(), version.getBuildNumber()};
		EventBuses.registerModEventBus(Origins.MODID, FMLJavaModLoadingContext.get().getModEventBus());
		Origins.register();
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> OriginsForgeClient::initialize);
		DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> OriginsServer::register);
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> CapabilityManager.INSTANCE.register(OriginComponent.class, new ModComponentsImpl.OriginStorage(), () -> null));
		MinecraftForge.EVENT_BUS.addListener((FMLServerAboutToStartEvent event) -> OriginsClient.isServerRunningOrigins = true);
		MinecraftForge.EVENT_BUS.addListener((FMLServerStoppedEvent event) -> OriginsClient.isServerRunningOrigins = false);
	}
}

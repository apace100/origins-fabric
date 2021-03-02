package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.ArtifactVersion;

@Mod(Origins.MODID)
public class OriginsForge {
	public OriginsForge() {
		ArtifactVersion version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion();
		Origins.VERSION = version.toString();
		Origins.SEMVER = new int[] {version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion(), version.getBuildNumber()};
		EventBuses.registerModEventBus(Origins.MODID, FMLJavaModLoadingContext.get().getModEventBus());
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> Origins.VERSION, OriginsForge::handleVersionCheck));
		Origins.register();
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> OriginsForgeClient::initialize);
		DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> OriginsServer::register);
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> CapabilityManager.INSTANCE.register(OriginComponent.class, new ModComponentsImpl.OriginStorage(), () -> null));
	}

	private static boolean handleVersionCheck(String version, boolean network) {
		//This is a hack to keep compatibility with the standard code
		return OriginsClient.isServerRunningOrigins = !network || Origins.VERSION.equals(version);
	}
}

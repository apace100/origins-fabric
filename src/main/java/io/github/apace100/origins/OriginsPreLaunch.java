package io.github.apace100.origins;

import net.fabricmc.loader.DependencyException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;

public class OriginsPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if(FabricLoader.getInstance().isModLoaded("connectormod") && !FabricLoader.getInstance().isModLoaded("connectorextras")) {
            Origins.LOGGER.error("=========================================");
            Origins.LOGGER.error("  Origins Mod Error: Missing Dependency\n");
            Origins.LOGGER.error("  The required mod \"Connector Extras\" is not installed.");
            Origins.LOGGER.error("  Please download and install Connector Extras to continue.");
            Origins.LOGGER.error("=========================================");
            throw new RuntimeException("Required Mod \"Connector Extras\" is not present");
        }
    }
}

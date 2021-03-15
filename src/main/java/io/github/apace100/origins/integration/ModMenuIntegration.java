package io.github.apace100.origins.integration;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public String getModId() {
        return Origins.MODID; // Return your modid here
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(OriginsClient.ClientConfig.class, parent).get();
    }
}
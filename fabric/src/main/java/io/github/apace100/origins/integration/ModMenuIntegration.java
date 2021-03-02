package io.github.apace100.origins.integration;

import io.github.apace100.origins.util.OriginsConfig;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    @Environment(EnvType.CLIENT)
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(OriginsConfig.class, parent).get();
    }
}
package io.github.apace100.origins.util;

import com.google.gson.Gson;
import io.github.apace100.origins.Origins;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class OriginsJsonConfigSerializer<T extends ConfigData> extends GsonConfigSerializer<T> {

    private final OriginsConfigSerializer<T> legacySerializer;
    private final Path configPath;

    public OriginsJsonConfigSerializer(Config definition, Class<T> configClass, Gson gson) {
        super(definition, configClass, gson);
        configPath = Utils.getConfigFolder().resolve(definition.name() + ".json5");
        legacySerializer = null;
    }

    public OriginsJsonConfigSerializer(Config definition, Class<T> configClass) {
        super(definition, configClass);
        configPath = Utils.getConfigFolder().resolve(definition.name() + ".json5");
        legacySerializer = null;
    }

    public OriginsJsonConfigSerializer(Config definition, Class<T> configClass, OriginsConfigSerializer<T> legacySerializer) {
        super(definition, configClass);
        configPath = Utils.getConfigFolder().resolve(definition.name() + ".json5");
        this.legacySerializer = legacySerializer;
    }

    @Override
    public T deserialize() {
        T t;
        // Try loading config from old serializer if it exists
        if(Files.exists(legacySerializer.getConfigPath()) && !Files.exists(configPath)) {
            try {
                t = legacySerializer.deserialize();
                serialize(t);
                Origins.LOGGER.info("Converted old .toml config to new .json5 format. Old file will be renamed to origins_server.toml.unused.");
                Files.move(legacySerializer.getConfigPath(), legacySerializer.getConfigPath().getParent().resolve("origins_server.toml.unused"));
                return t;
            } catch (Exception e) {
                Origins.LOGGER.error("Failed converting old .toml config to new .json5 format: " + e.getMessage());
            }
        }

        try {
            t = super.deserialize();
        } catch(Exception e) {
            Origins.LOGGER.error("Failed reading config (re-creating default): " + e.getMessage());
            t = super.createDefault();
        }

        return t;
    }
}

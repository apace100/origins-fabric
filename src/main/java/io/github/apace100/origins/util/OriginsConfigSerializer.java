package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;

import java.nio.file.Path;

public class OriginsConfigSerializer<T extends ConfigData> extends Toml4jConfigSerializer<T> {

    private final Path configPath;

    public OriginsConfigSerializer(Config definition, Class<T> configClass, TomlWriter tomlWriter) {
        super(definition, configClass, tomlWriter);
        configPath = Utils.getConfigFolder().resolve(definition.name() + ".toml");
    }

    public OriginsConfigSerializer(Config definition, Class<T> configClass) {
        super(definition, configClass);
        configPath = Utils.getConfigFolder().resolve(definition.name() + ".toml");
    }

    public Path getConfigPath() {
        return configPath;
    }

    @Override
    public T deserialize() {
        T t;
        try {
            t = super.deserialize();
        } catch(Exception e) {
            Origins.LOGGER.error("Failed reading config (re-creating default): " + e.getMessage());
            return super.createDefault();
        }
        return t;
    }
}

package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;

public class OriginsConfigSerializer<T extends ConfigData> extends Toml4jConfigSerializer<T> {

    public OriginsConfigSerializer(Config definition, Class<T> configClass, TomlWriter tomlWriter) {
        super(definition, configClass, tomlWriter);
    }

    public OriginsConfigSerializer(Config definition, Class<T> configClass) {
        super(definition, configClass);
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

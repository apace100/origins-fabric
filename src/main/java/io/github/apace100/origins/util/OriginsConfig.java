package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.com.moandjiezana.toml.TomlWriter;

@Config(name = Origins.MODID)
public class OriginsConfig implements ConfigData {

    public int xOffset = 0;
    public int yOffset = 0;

    public float phantomizedOverlayStrength = 0.8F;

    @Override
    public void validatePostLoad() throws ValidationException {
        if(phantomizedOverlayStrength < 0F) {
            phantomizedOverlayStrength = 0F;
        } else if(phantomizedOverlayStrength > 1F) {
            phantomizedOverlayStrength = 1F;
        }
    }

    public static class Serializer<T extends ConfigData> extends Toml4jConfigSerializer<T> {

        public Serializer(Config definition, Class<T> configClass, TomlWriter tomlWriter) {
            super(definition, configClass, tomlWriter);
        }

        public Serializer(Config definition, Class<T> configClass) {
            super(definition, configClass);
        }

        @Override
        public T deserialize() throws SerializationException {
            T t;
            try {
                t = super.deserialize();
            } catch(Exception e) {
                Origins.LOGGER.error("Failed reading config (creating default): " + e.getMessage());
                return super.createDefault();
            }
            return t;
        }
    }
}

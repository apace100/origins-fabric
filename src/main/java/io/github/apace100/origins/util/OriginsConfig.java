package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

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
}

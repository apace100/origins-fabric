package io.github.apace100.origins.power;

import io.github.apace100.origins.util.HudRender;

public interface HudRendered {

    HudRender getRenderSettings();
    float getFill();
    boolean shouldRender();
}

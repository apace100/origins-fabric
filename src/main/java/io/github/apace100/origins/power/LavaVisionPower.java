package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class LavaVisionPower extends Power {

    private final float s;
    private final float v;

    public LavaVisionPower(PowerType<?> type, PlayerEntity player, float s, float v) {
        super(type, player);
        this.s = s;
        this.v = v;
    }

    public float getS() {
        return s;
    }

    public float getV() {
        return v;
    }
}

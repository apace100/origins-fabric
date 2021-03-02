package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ShaderPower extends Power {

    private final Identifier shaderLocation;

    public ShaderPower(PowerType<?> type, PlayerEntity player, Identifier shaderLocation) {
        super(type, player);
        this.shaderLocation = shaderLocation;
    }

    public Identifier getShaderLocation() {
        return shaderLocation;
    }
}

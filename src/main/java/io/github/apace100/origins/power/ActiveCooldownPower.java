package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class ActiveCooldownPower extends CooldownPower implements Active {

    private final Consumer<PlayerEntity> activeFunction;

    public ActiveCooldownPower(PowerType<?> type, PlayerEntity player, int cooldownDuration, int barIndex, Consumer<PlayerEntity> activeFunction) {
        super(type, player, cooldownDuration, barIndex);
        this.activeFunction = activeFunction;
    }

    @Override
    public void onUse() {
        if(canUse()) {
            this.activeFunction.accept(this.player);
            use();
        }
    }
}

package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class HudRender {

    public static final HudRender DONT_RENDER = new HudRender(false, 0, Origins.identifier("textures/gui/resource_bar.png"), null);

    private final boolean shouldRender;
    private final int barIndex;
    private final Identifier spriteLocation;
    private final ConditionFactory<LivingEntity>.Instance playerCondition;

    public HudRender(boolean shouldRender, int barIndex, Identifier spriteLocation, ConditionFactory<LivingEntity>.Instance condition) {
        this.shouldRender = shouldRender;
        this.barIndex = barIndex;
        this.spriteLocation = spriteLocation;
        this.playerCondition = condition;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public int getBarIndex() {
        return barIndex;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public boolean shouldRender(PlayerEntity player) {
        return shouldRender && (playerCondition == null || playerCondition.test(player));
    }

    public ConditionFactory<LivingEntity>.Instance getCondition() {
        return playerCondition;
    }
}

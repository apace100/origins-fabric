package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public class CooldownPower extends Power implements HudRendered {

    private long lastUseTime;

    public final int cooldownDuration;
    private final int barIndex;
    private final boolean shouldRender;

    public CooldownPower(PowerType<?> type, PlayerEntity player, int cooldownDuration, int barIndex) {
        this(type, player, cooldownDuration, barIndex, true);
    }

    public CooldownPower(PowerType<?> type, PlayerEntity player, int cooldownDuration, int barIndex, boolean shouldRender) {
        super(type, player);
        this.cooldownDuration = cooldownDuration;
        this.barIndex = barIndex;
        this.shouldRender = shouldRender;
    }

    public boolean canUse() {
        return player.getEntityWorld().getTime() >= lastUseTime + cooldownDuration;
    }

    public void use() {
        lastUseTime = player.getEntityWorld().getTime();
        OriginComponent.sync(player);
    }

    public float getProgress() {
        float time = player.getEntityWorld().getTime() - lastUseTime;
        return Math.min(1F, Math.max(time / (float)cooldownDuration, 0F));
    }

    @Override
    public Tag toTag() {
        return LongTag.of(lastUseTime);
    }

    @Override
    public void fromTag(Tag tag) {
        lastUseTime = ((LongTag)tag).getLong();
    }

    @Override
    public int getBarIndex() {
        return barIndex;
    }

    @Override
    public float getFill() {
        return getProgress();
    }

    @Override
    public boolean shouldRender() {
        return shouldRender && barIndex > 0 && (player.getEntityWorld().getTime() - lastUseTime) <= cooldownDuration;
    }
}

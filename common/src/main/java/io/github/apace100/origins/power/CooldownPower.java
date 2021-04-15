package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public class CooldownPower extends Power implements HudRendered {

    private long lastUseTime;

    public final int cooldownDuration;
    private final HudRender hudRender;

    public CooldownPower(PowerType<?> type, PlayerEntity player, int cooldownDuration, HudRender hudRender) {
        super(type, player);
        this.cooldownDuration = cooldownDuration;
        this.hudRender = hudRender;
    }

    public boolean canUse() {
        return player.getEntityWorld().getTime() >= lastUseTime + cooldownDuration && isActive();
    }

    public void use() {
        lastUseTime = player.getEntityWorld().getTime();
        OriginComponent.sync(player);
    }

    public float getProgress() {
        float time = player.getEntityWorld().getTime() - lastUseTime;
        return Math.min(1F, Math.max(time / (float)cooldownDuration, 0F));
    }

    public int getRemainingTicks() {
        return (int)Math.max(0, cooldownDuration - (player.getEntityWorld().getTime() - lastUseTime));
    }

    public void modify(int changeInTicks){
        this.lastUseTime += changeInTicks;
        long currentTime = player.getEntityWorld().getTime();
        if(this.lastUseTime > currentTime) {
            lastUseTime = currentTime;
        }
    }

    public void setCooldown(int cooldownInTicks) {
        long currentTime = player.getEntityWorld().getTime();
        this.lastUseTime = currentTime - Math.min(cooldownInTicks, cooldownDuration);
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
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        return getProgress();
    }

    @Override
    public boolean shouldRender() {
        return (player.getEntityWorld().getTime() - lastUseTime) <= cooldownDuration;
    }
}

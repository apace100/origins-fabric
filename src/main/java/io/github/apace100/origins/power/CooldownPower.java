package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public class CooldownPower extends Power {

    public final int cooldownDuration;

    private long lastUseTime;

    public CooldownPower(PowerType<?> type, PlayerEntity player, int cooldownDuration) {
        super(type, player);
        this.cooldownDuration = cooldownDuration;
    }

    public boolean canUse() {
        return player.getEntityWorld().getTime() >= lastUseTime + cooldownDuration;
    }

    public void use() {
        lastUseTime = player.getEntityWorld().getTime();
    }

    public float getProgress() {
        float time = (int)(player.getEntityWorld().getTime() - lastUseTime);
        return Math.min(1F, Math.max(time / cooldownDuration, 0F));
    }

    @Override
    public Tag toTag() {
        return LongTag.of(lastUseTime);
    }

    @Override
    public void fromTag(Tag tag) {
        lastUseTime = ((LongTag)tag).getLong();
    }
}

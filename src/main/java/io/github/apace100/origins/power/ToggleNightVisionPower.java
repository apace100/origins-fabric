package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;

public class ToggleNightVisionPower extends NightVisionPower implements Active {
    private boolean isActive;

    public ToggleNightVisionPower(PowerType<?> type, PlayerEntity player) {
        this(type, player, 1.0F, true);
    }

    public ToggleNightVisionPower(PowerType<?> type, PlayerEntity player, float strength, boolean activeByDefault) {
        super(type, player, strength);
        this.isActive = activeByDefault;
    }

    @Override
    public void onUse() {
        this.isActive = !this.isActive;
        OriginComponent.sync(player);
    }

    public boolean isActive() {
        return this.isActive && super.isActive();
    }

    @Override
    public Tag toTag() {
        return ByteTag.of(isActive);
    }

    @Override
    public void fromTag(Tag tag) {
        if(tag instanceof ByteTag) {
            isActive = ((ByteTag)tag).getByte() > 0;
        }
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }
}

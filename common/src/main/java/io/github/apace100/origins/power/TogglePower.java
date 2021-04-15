package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;

public class TogglePower extends Power implements Active {

    private boolean isActive;

    public TogglePower(PowerType<?> type, PlayerEntity player) {
        this(type, player, false);
    }

    public TogglePower(PowerType<?> type, PlayerEntity player, boolean activeByDefault) {
        super(type, player);
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
        isActive = ((ByteTag)tag).getByte() > 0;
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

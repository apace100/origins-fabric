package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;

public class TogglePower extends Power implements Active {

    private boolean isActive;

    public TogglePower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
        this.isActive = true;
    }

    @Override
    public void onUse() {
        this.isActive = !this.isActive;
        OriginComponent.sync(player);
    }

    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public Tag toTag() {
        return ByteTag.of(isActive);
    }

    @Override
    public void fromTag(Tag tag) {
        isActive = ((ByteTag)tag).getByte() > 0;
    }
}

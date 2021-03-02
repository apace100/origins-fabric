package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;

import java.util.function.Consumer;

public class ActionOverTimePower extends Power {

    private final int interval;
    private final Consumer<Entity> entityAction;
    private final Consumer<Entity> risingAction;
    private final Consumer<Entity> fallingAction;

    private boolean wasActive = false;

    public ActionOverTimePower(PowerType<?> type, PlayerEntity player, int interval, Consumer<Entity> entityAction, Consumer<Entity> risingAction, Consumer<Entity> fallingAction) {
        super(type, player);
        this.interval = interval;
        this.entityAction = entityAction;
        this.risingAction = risingAction;
        this.fallingAction = fallingAction;
        this.setTicking(true);
    }

    public void tick() {
        if(player.age % interval == 0) {
            if (isActive()) {
                if (entityAction != null) {
                    entityAction.accept(player);
                }
                if (!wasActive && risingAction != null) {
                    risingAction.accept(player);
                }
                wasActive = true;
            } else {
                if (wasActive && fallingAction != null) {
                    fallingAction.accept(player);
                }
                wasActive = false;
            }
        }
    }

    @Override
    public Tag toTag() {
        return ByteTag.of(wasActive);
    }

    @Override
    public void fromTag(Tag tag) {
        wasActive = tag.equals(ByteTag.ONE);
    }
}

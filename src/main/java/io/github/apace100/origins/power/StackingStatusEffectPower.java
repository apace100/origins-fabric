package io.github.apace100.origins.power;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

public class StackingStatusEffectPower extends StatusEffectPower {

    private final int minStack;
    private final int maxStack;
    private final int durationPerStack;

    private int currentStack;

    public StackingStatusEffectPower(PowerType<?> type, PlayerEntity player, int minStack, int maxStack, int durationPerStack) {
        super(type, player);
        this.minStack = minStack;
        this.maxStack = maxStack;
        this.durationPerStack = durationPerStack;
    }

    public void tick() {
        if(isActive()) {
            currentStack += 1;
            if(currentStack > maxStack) {
                currentStack = maxStack;
            }
            if(currentStack > 0) {
                applyEffects();
            }
        } else {
            currentStack -= 1;
            if(currentStack < minStack) {
                currentStack = minStack;
            }
        }
    }

    @Override
    public void applyEffects() {
        effects.forEach(sei -> {
            int duration = durationPerStack * currentStack;
            if(duration > 0) {
                StatusEffectInstance applySei = new StatusEffectInstance(sei.getEffectType(), duration, sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                player.addStatusEffect(applySei);
            }
        });
    }

    @Override
    public Tag toTag() {
        return IntTag.of(currentStack);
    }

    @Override
    public void fromTag(Tag tag) {
        currentStack = ((IntTag)tag).getInt();
    }
}

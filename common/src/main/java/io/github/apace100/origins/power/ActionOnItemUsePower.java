package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnItemUsePower extends Power {

    private final Predicate<ItemStack> itemCondition;
    private final Consumer<Entity> entityAction;
    private final Consumer<ItemStack> itemAction;

    public ActionOnItemUsePower(PowerType<?> type, PlayerEntity player, Predicate<ItemStack> itemCondition, Consumer<Entity> entityAction, Consumer<ItemStack> itemAction) {
        super(type, player);
        this.itemCondition = itemCondition;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(stack);
    }

    public void executeActions(ItemStack stack) {
        if(itemAction != null) {
            itemAction.accept(stack);
        }
        if(entityAction != null) {
            entityAction.accept(player);
        }
    }
}

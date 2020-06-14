package io.github.apace100.origins.power;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.TypedActionResult;

import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<ItemStack> predicate;

    public PreventItemUsePower(PowerType<?> type, PlayerEntity player, Predicate<ItemStack> predicate) {
        super(type, player);
        this.predicate = predicate;
        /*UseItemCallback.EVENT.register(((playerEntity, world, hand) -> {
            if(getType().isActive(playerEntity)) {
                ItemStack stackInHand = playerEntity.getStackInHand(hand);
                if(doesPrevent(stackInHand)) {
                    return TypedActionResult.fail(stackInHand);
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        }));*/
    }

    public boolean doesPrevent(ItemStack stack) {
        return predicate.test(stack);
    }
}

package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

public class ModifyFoodPower extends Power {

    private final Predicate<ItemStack> applicableFood;
    private final Function<FoodComponent, Integer> foodLevel;
    private final Function<FoodComponent, Float> saturation;

    public ModifyFoodPower(PowerType<?> type, PlayerEntity player, Predicate<ItemStack> applicableFood, Function<FoodComponent, Integer> additionalFood, Function<FoodComponent, Float> additionalSaturation) {
        super(type, player);
        this.applicableFood = applicableFood;
        this.foodLevel = additionalFood;
        this.saturation = additionalSaturation;
    }

    public boolean doesApply(ItemStack stack) {
        return applicableFood.test(stack);
    }

    public int getFoodLevel(FoodComponent food) {
        return foodLevel.apply(food);
    }

    public float getSaturation(FoodComponent food) {
        return saturation.apply(food);
    }
}

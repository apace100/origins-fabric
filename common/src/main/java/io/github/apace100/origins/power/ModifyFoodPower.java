package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyFoodPower extends Power {

    private final Predicate<ItemStack> applicableFood;
    private final List<EntityAttributeModifier> foodModifiers;
    private final List<EntityAttributeModifier> saturationModifiers;
    private final Consumer<Entity> entityActionWhenEaten;

    public ModifyFoodPower(PowerType<?> type, PlayerEntity player, Predicate<ItemStack> applicableFood, List<EntityAttributeModifier> foodModifiers, List<EntityAttributeModifier> saturationModifiers, Consumer<Entity> entityActionWhenEaten) {
        super(type, player);
        this.applicableFood = applicableFood;
        this.foodModifiers = foodModifiers;
        this.saturationModifiers = saturationModifiers;
        this.entityActionWhenEaten = entityActionWhenEaten;
    }

    public boolean doesApply(ItemStack stack) {
        return applicableFood.test(stack);
    }

    public void eat() {
        if(entityActionWhenEaten != null) {
            entityActionWhenEaten.accept(player);
        }
    }

    public List<EntityAttributeModifier> getFoodModifiers() {
        return foodModifiers;
    }

    public List<EntityAttributeModifier> getSaturationModifiers() {
        return saturationModifiers;
    }
}

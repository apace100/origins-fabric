package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.ModifyFoodPower;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.AttributeUtil;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Unique
    private PlayerEntity player;

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getHunger()I"))
    private int modifyHunger(FoodComponent foodComponent, Item item, ItemStack stack) {
        if(player != null) {
            double baseValue = foodComponent.getHunger();
        List<EntityAttributeModifier> modifiers = ModComponents.ORIGIN.get(player).getPowers(ModifyFoodPower.class).stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getFoodModifiers().stream()).collect(Collectors.toList());
            return (int)AttributeUtil.sortAndApplyModifiers(modifiers, baseValue);
        }
        return foodComponent.getHunger();
    }

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getSaturationModifier()F"))
    private float modifySaturation(FoodComponent foodComponent, Item item, ItemStack stack) {
        if(player != null) {
            double baseValue = foodComponent.getSaturationModifier();
            List<EntityAttributeModifier> modifiers = ModComponents.ORIGIN.get(player).getPowers(ModifyFoodPower.class).stream()
                .filter(p -> p.doesApply(stack))
                .flatMap(p -> p.getSaturationModifiers().stream()).collect(Collectors.toList());
            return (float) AttributeUtil.sortAndApplyModifiers(modifiers, baseValue);
        }
        return foodComponent.getSaturationModifier();
    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", shift = At.Shift.AFTER))
    private void executeAdditionalEatAction(Item item, ItemStack stack, CallbackInfo ci) {
        if(player != null) {
            ModComponents.ORIGIN.get(player).getPowers(ModifyFoodPower.class).stream().filter(p -> p.doesApply(stack)).forEach(ModifyFoodPower::eat);
        }
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void cachePlayer(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }
}

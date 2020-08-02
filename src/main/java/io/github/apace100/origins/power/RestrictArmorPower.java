package io.github.apace100.origins.power;

import net.minecraft.data.client.model.BlockStateVariantMap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class RestrictArmorPower extends Power {
    private final BlockStateVariantMap.TriFunction<ItemStack, PlayerEntity, EquipmentSlot, Boolean> unusableArmorFunction;

    public RestrictArmorPower(PowerType<?> type, PlayerEntity player, Predicate<ItemStack> unusableArmorPredicate) {
        this(type, player, (itemStack, playerEntity, equipmentSlot) -> unusableArmorPredicate.test(itemStack));
    }

    public RestrictArmorPower(PowerType<?> type, PlayerEntity player, BlockStateVariantMap.TriFunction<ItemStack, PlayerEntity, EquipmentSlot, Boolean> unusableArmorFunction) {
        super(type, player);
        this.unusableArmorFunction = unusableArmorFunction;
    }

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        return !unusableArmorFunction.apply(itemStack, player, slot);
    }
}

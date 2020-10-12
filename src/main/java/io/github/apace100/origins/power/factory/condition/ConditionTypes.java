package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

public class ConditionTypes {

    public static ConditionType<PlayerEntity> PLAYER = new ConditionType<>("PlayerCondition", ModRegistries.PLAYER_CONDITION);
    public static ConditionType<ItemStack> ITEM = new ConditionType<>("ItemCondition", ModRegistries.ITEM_CONDITION);
    public static ConditionType<CachedBlockPosition> BLOCK = new ConditionType<>("BlockCondition", ModRegistries.BLOCK_CONDITION);
    public static ConditionType<Pair<DamageSource, Float>> DAMAGE = new ConditionType<>("DamageCondition", ModRegistries.DAMAGE_CONDITION);

}

package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.registry.ModRegistriesArchitectury;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.biome.Biome;

public class ConditionTypes {

    public static ConditionType<LivingEntity> ENTITY = new ConditionType<>("EntityCondition", ModRegistriesArchitectury.ENTITY_CONDITION);
    public static ConditionType<ItemStack> ITEM = new ConditionType<>("ItemCondition", ModRegistriesArchitectury.ITEM_CONDITION);
    public static ConditionType<CachedBlockPosition> BLOCK = new ConditionType<>("BlockCondition", ModRegistriesArchitectury.BLOCK_CONDITION);
    public static ConditionType<Pair<DamageSource, Float>> DAMAGE = new ConditionType<>("DamageCondition", ModRegistriesArchitectury.DAMAGE_CONDITION);
    public static ConditionType<FluidState> FLUID = new ConditionType<>("FluidCondition", ModRegistriesArchitectury.FLUID_CONDITION);
    public static ConditionType<Biome> BIOME = new ConditionType<>("BiomeCondition", ModRegistriesArchitectury.BIOME_CONDITION);

}

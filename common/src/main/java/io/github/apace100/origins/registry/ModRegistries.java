package io.github.apace100.origins.registry;

import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.power.factory.action.ActionFactory;
import io.github.apace100.origins.util.VanillaWrappedRegistry;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Triple;

/**
 * This class only exists as a backward compatibility option,
 * it shouldn't actually be used.
 * @deprecated Use {@link ModRegistriesArchitectury} instead.
 */
@Deprecated
public class ModRegistries {

	@Deprecated
	public static final Registry<PowerFactory<?>> POWER_FACTORY;
	@Deprecated
	public static final Registry<ConditionFactory<LivingEntity>> ENTITY_CONDITION;
	@Deprecated
	public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
	@Deprecated
	public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
	@Deprecated
	public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
	@Deprecated
	public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
	@Deprecated
	public static final Registry<ConditionFactory<Biome>> BIOME_CONDITION;
	@Deprecated
	public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
	@Deprecated
	public static final Registry<ActionFactory<ItemStack>> ITEM_ACTION;
	@Deprecated
	public static final Registry<ActionFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION;

	static {
		POWER_FACTORY = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.POWER_FACTORY);
		ENTITY_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.ENTITY_CONDITION, ModRegistriesArchitectury.CFEntity::new, ModRegistriesArchitectury.CFEntity::get);
		ITEM_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.ITEM_CONDITION, ModRegistriesArchitectury.CFItem::new, ModRegistriesArchitectury.CFItem::get);
		BLOCK_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.BLOCK_CONDITION, ModRegistriesArchitectury.CFBlock::new, ModRegistriesArchitectury.CFBlock::get);
		DAMAGE_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.DAMAGE_CONDITION, ModRegistriesArchitectury.CFDamage::new, ModRegistriesArchitectury.CFDamage::get);
		FLUID_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.FLUID_CONDITION, ModRegistriesArchitectury.CFFluid::new, ModRegistriesArchitectury.CFFluid::get);
		BIOME_CONDITION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.BIOME_CONDITION, ModRegistriesArchitectury.CFBiome::new, ModRegistriesArchitectury.CFBiome::get);
		ENTITY_ACTION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.ENTITY_ACTION, ModRegistriesArchitectury.AFEntity::new, ModRegistriesArchitectury.AFEntity::get);
		ITEM_ACTION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.ITEM_ACTION, ModRegistriesArchitectury.AFItem::new, ModRegistriesArchitectury.AFItem::get);
		BLOCK_ACTION = VanillaWrappedRegistry.wrap(ModRegistriesArchitectury.BLOCK_ACTION, ModRegistriesArchitectury.AFBlock::new, ModRegistriesArchitectury.AFBlock::get);
	}
}

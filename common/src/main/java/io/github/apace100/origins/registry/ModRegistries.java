package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.power.factory.action.ActionFactory;
import io.github.apace100.origins.util.WrappedRegistry;
import me.shedaniel.architectury.registry.Registries;
import me.shedaniel.architectury.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Triple;

import static net.minecraft.util.registry.Registry.*;

public class ModRegistries {

    public static final Lazy<Registries> REGISTRIES = new Lazy<>(() -> Registries.get(Origins.MODID));

    public static final Registry<PowerFactory<?>> POWER_FACTORY;
    public static final Registry<ConditionFactory<LivingEntity>> ENTITY_CONDITION;
    public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
    public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
    public static final Registry<ConditionFactory<Biome>> BIOME_CONDITION;
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<ItemStack>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION;

    public static final Registry<Item> ITEMS;
    public static final Registry<Block> BLOCKS;
    public static final Registry<EntityType<?>> ENTITY_TYPES;
    public static final Registry<Enchantment> ENCHANTMENTS;
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZERS;

    static {
        Registries registries = REGISTRIES.get();
        Registry<CFEntity> entityCondition = registries.<CFEntity>builder(Origins.identifier("entity_condition")).build();
        Registry<CFItem> itemCondition = registries.<CFItem>builder(Origins.identifier("item_condition")).build();
        Registry<CFBlock> blockCondition = registries.<CFBlock>builder(Origins.identifier("block_condition")).build();
        Registry<CFDamage> damageCondition = registries.<CFDamage>builder(Origins.identifier("damage_condition")).build();
        Registry<CFFluid> fluidCondition = registries.<CFFluid>builder(Origins.identifier("fluid_condition")).build();
        Registry<CFBiome> biomeCondition = registries.<CFBiome>builder(Origins.identifier("biome_condition")).build();
        Registry<AFEntity> entityAction = registries.<AFEntity>builder(Origins.identifier("entity_action")).build();
        Registry<AFItem> itemAction = registries.<AFItem>builder(Origins.identifier("item_action")).build();
        Registry<AFBlock> blockAction = registries.<AFBlock>builder(Origins.identifier("block_action")).build();

        POWER_FACTORY = registries.<PowerFactory<?>>builder(new Identifier(Origins.MODID, "power_factory")).build();
        ENTITY_CONDITION = new WrappedRegistry<>(entityCondition, CFEntity::new, CFEntity::get);
        ITEM_CONDITION = new WrappedRegistry<>(itemCondition, CFItem::new, CFItem::get);
        BLOCK_CONDITION = new WrappedRegistry<>(blockCondition, CFBlock::new, CFBlock::get);
        DAMAGE_CONDITION = new WrappedRegistry<>(damageCondition, CFDamage::new, CFDamage::get);
        FLUID_CONDITION = new WrappedRegistry<>(fluidCondition, CFFluid::new, CFFluid::get);
        BIOME_CONDITION = new WrappedRegistry<>(biomeCondition, CFBiome::new, CFBiome::get);
        ENTITY_ACTION = new WrappedRegistry<>(entityAction, AFEntity::new, AFEntity::get);
        ITEM_ACTION = new WrappedRegistry<>(itemAction, AFItem::new, AFItem::get);
        BLOCK_ACTION = new WrappedRegistry<>(blockAction, AFBlock::new, AFBlock::get);

        ITEMS = registries.get(ITEM_KEY);
        BLOCKS = registries.get(BLOCK_KEY);
        ENTITY_TYPES = registries.get(ENTITY_TYPE_KEY);
        ENCHANTMENTS = registries.get(ENCHANTMENT_KEY);
        RECIPE_SERIALIZERS = registries.get(RECIPE_SERIALIZER_KEY);
    }

    private static class CFEntity extends WrappedRegistry.Wrapper<ConditionFactory<LivingEntity>, CFEntity> { public CFEntity(ConditionFactory<LivingEntity> value) { super(value); }}
    private static class CFItem extends WrappedRegistry.Wrapper<ConditionFactory<ItemStack>, CFItem> { public CFItem(ConditionFactory<ItemStack> value) { super(value); }}
    private static class CFBlock extends WrappedRegistry.Wrapper<ConditionFactory<CachedBlockPosition>, CFBlock> { public CFBlock(ConditionFactory<CachedBlockPosition> value) { super(value); }}
    private static class CFDamage extends WrappedRegistry.Wrapper<ConditionFactory<Pair<DamageSource, Float>>, CFDamage> { public CFDamage(ConditionFactory<Pair<DamageSource, Float>> value) { super(value); }}
    private static class CFFluid extends WrappedRegistry.Wrapper<ConditionFactory<FluidState>, CFFluid> { public CFFluid(ConditionFactory<FluidState> value) { super(value); }}
    private static class CFBiome extends WrappedRegistry.Wrapper<ConditionFactory<Biome>, CFBiome> { public CFBiome(ConditionFactory<Biome> value) { super(value); }}

    private static class AFEntity extends WrappedRegistry.Wrapper<ActionFactory<Entity>, AFEntity> { public AFEntity(ActionFactory<Entity> value) { super(value); }}
    private static class AFItem extends WrappedRegistry.Wrapper<ActionFactory<ItemStack>, AFItem> { public AFItem(ActionFactory<ItemStack> value) { super(value); }}
    private static class AFBlock extends WrappedRegistry.Wrapper<ActionFactory<Triple<World, BlockPos, Direction>>, AFBlock> { public AFBlock(ActionFactory<Triple<World, BlockPos, Direction>> value) { super(value); }}
}

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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import static net.minecraft.util.registry.Registry.*;

public class ModRegistries {

    public static final Lazy<Registries> REGISTRIES = new Lazy<>(() -> Registries.get(Origins.MODID));

    public static final Registry<PowerFactory<?>> POWER_FACTORY;
    public static final Registry<ConditionFactory<PlayerEntity>> PLAYER_CONDITION;
    public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
    public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<ItemStack>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION;

    public static final Registry<Item> ITEMS;
    public static final Registry<Block> BLOCKS;
    public static final Registry<EntityType<?>> ENTITY_TYPES;
    public static final Registry<Enchantment> ENCHANTMENTS;

    static {
        Registries registries = REGISTRIES.get();
        Registry<CFP> playerCondition = registries.<CFP>builder(Origins.identifier("player_condition")).build();
        Registry<CFI> itemCondition = registries.<CFI>builder(Origins.identifier("item_condition")).build();
        Registry<CFB> blockCondition = registries.<CFB>builder(Origins.identifier("block_condition")).build();
        Registry<CFD> damageCondition = registries.<CFD>builder(Origins.identifier("damage_condition")).build();
        Registry<CFF> fluidCondition = registries.<CFF>builder(Origins.identifier("fluid_condition")).build();
        Registry<AFE> entityAction = registries.<AFE>builder(Origins.identifier("entity_action")).build();
        Registry<AFI> itemAction = registries.<AFI>builder(Origins.identifier("item_action")).build();
        Registry<AFB> blockAction = registries.<AFB>builder(Origins.identifier("block_action")).build();

        POWER_FACTORY = registries.<PowerFactory<?>>builder(new Identifier(Origins.MODID, "power_factory")).build();
        PLAYER_CONDITION = new WrappedRegistry<>(playerCondition, CFP::new, CFP::get);
        ITEM_CONDITION = new WrappedRegistry<>(itemCondition, CFI::new, CFI::get);
        BLOCK_CONDITION = new WrappedRegistry<>(blockCondition, CFB::new, CFB::get);
        DAMAGE_CONDITION = new WrappedRegistry<>(damageCondition, CFD::new, CFD::get);
        FLUID_CONDITION = new WrappedRegistry<>(fluidCondition, CFF::new, CFF::get);
        ENTITY_ACTION = new WrappedRegistry<>(entityAction, AFE::new, AFE::get);
        ITEM_ACTION = new WrappedRegistry<>(itemAction, AFI::new, AFI::get);
        BLOCK_ACTION = new WrappedRegistry<>(blockAction, AFB::new, AFB::get);

        ITEMS = registries.get(ITEM_KEY);
        BLOCKS = registries.get(BLOCK_KEY);
        ENTITY_TYPES = registries.get(ENTITY_TYPE_KEY);
        ENCHANTMENTS = registries.get(ENCHANTMENT_KEY);
    }

    public static class CFP extends WrappedRegistry.Wrapper<ConditionFactory<PlayerEntity>, CFP> { public CFP(ConditionFactory<PlayerEntity> value) { super(value); }}
    public static class CFI extends WrappedRegistry.Wrapper<ConditionFactory<ItemStack>, CFI> { public CFI(ConditionFactory<ItemStack> value) { super(value); }}
    public static class CFB extends WrappedRegistry.Wrapper<ConditionFactory<CachedBlockPosition>, CFB> { public CFB(ConditionFactory<CachedBlockPosition> value) { super(value); }}
    public static class CFD extends WrappedRegistry.Wrapper<ConditionFactory<Pair<DamageSource, Float>>, CFD> { public CFD(ConditionFactory<Pair<DamageSource, Float>> value) { super(value); }}
    public static class CFF extends WrappedRegistry.Wrapper<ConditionFactory<FluidState>, CFF> { public CFF(ConditionFactory<FluidState> value) { super(value); }}

    public static class AFE extends WrappedRegistry.Wrapper<ActionFactory<Entity>, AFE> { public AFE(ActionFactory<Entity> value) { super(value); }}
    public static class AFI extends WrappedRegistry.Wrapper<ActionFactory<ItemStack>, AFI> { public AFI(ActionFactory<ItemStack> value) { super(value); }}
    public static class AFB extends WrappedRegistry.Wrapper<ActionFactory<Triple<World, BlockPos, Direction>>, AFB> { public AFB(ActionFactory<Triple<World, BlockPos, Direction>> value) { super(value); }}
}

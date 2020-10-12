package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.util.ClassUtil;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

public class ModRegistries {

    public static final Registry<PowerFactory> POWER_FACTORY;
    public static final Registry<ConditionFactory<PlayerEntity>> PLAYER_CONDITION;
    public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;

    static {
        POWER_FACTORY = FabricRegistryBuilder.createSimple(PowerFactory.class, new Identifier(Origins.MODID, "power_factory")).buildAndRegister();
        PLAYER_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<PlayerEntity>>castClass(ConditionFactory.class), Origins.identifier("player_condition")).buildAndRegister();
        ITEM_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<ItemStack>>castClass(ConditionFactory.class), Origins.identifier("item_condition")).buildAndRegister();
        BLOCK_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<CachedBlockPosition>>castClass(ConditionFactory.class), Origins.identifier("block_condition")).buildAndRegister();
        DAMAGE_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<Pair<DamageSource, Float>>>castClass(ConditionFactory.class), Origins.identifier("damage_condition")).buildAndRegister();
    }
}

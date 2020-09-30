package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.power.factory.condition.block.BlockCondition;
import io.github.apace100.origins.power.factory.condition.damage.DamageCondition;
import io.github.apace100.origins.power.factory.condition.item.ItemCondition;
import io.github.apace100.origins.power.factory.condition.player.PlayerCondition;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRegistries {

    public static final Registry<PowerFactory.Serializer> POWER_FACTORY_SERIALIZER;
    public static final Registry<PlayerCondition.Serializer> PLAYER_CONDITION_SERIALIZER;
    public static final Registry<ItemCondition.Serializer> ITEM_CONDITION_SERIALIZER;
    public static final Registry<BlockCondition.Serializer> BLOCK_CONDITION_SERIALIZER;
    public static final Registry<DamageCondition.Serializer> DAMAGE_CONDITION_SERIALIZER;

    static {
        POWER_FACTORY_SERIALIZER = FabricRegistryBuilder.createSimple(PowerFactory.Serializer.class, new Identifier(Origins.MODID, "power_factory_serializer")).buildAndRegister();
        PLAYER_CONDITION_SERIALIZER = FabricRegistryBuilder.createSimple(PlayerCondition.Serializer.class, new Identifier(Origins.MODID, "player_condition_serializer")).buildAndRegister();
        ITEM_CONDITION_SERIALIZER = FabricRegistryBuilder.createSimple(ItemCondition.Serializer.class, new Identifier(Origins.MODID, "item_condition_serializer")).buildAndRegister();
        BLOCK_CONDITION_SERIALIZER = FabricRegistryBuilder.createSimple(BlockCondition.Serializer.class, new Identifier(Origins.MODID, "block_condition_serializer")).buildAndRegister();
        DAMAGE_CONDITION_SERIALIZER = FabricRegistryBuilder.createSimple(DamageCondition.Serializer.class, new Identifier(Origins.MODID, "damage_condition_serializer")).buildAndRegister();
    }
}

package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.registry.ModRegistriesArchitectury;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class ActionTypes {

    public static ActionType<Entity> ENTITY = new ActionType<>("EntityAction", ModRegistriesArchitectury.ENTITY_ACTION);
    public static ActionType<ItemStack> ITEM = new ActionType<>("ItemAction", ModRegistriesArchitectury.ITEM_ACTION);
    public static ActionType<Triple<World, BlockPos, Direction>> BLOCK = new ActionType<>("BlockAction", ModRegistriesArchitectury.BLOCK_ACTION);

}

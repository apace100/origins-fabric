package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class ModifyBreakSpeedPower extends FloatModifyingPower {

    private final Predicate<CachedBlockPosition> predicate;

    public ModifyBreakSpeedPower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> predicate, EntityAttributeModifier modifier) {
        super(type, player, modifier);
        this.predicate = predicate;
    }

    public boolean doesApply(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, false);
        return predicate.test(cbp);
    }
}

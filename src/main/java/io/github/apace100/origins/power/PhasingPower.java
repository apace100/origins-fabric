package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class PhasingPower extends Power {

    private final Predicate<CachedBlockPosition> blocks;
    private final boolean isBlacklist;

    private final Predicate<PlayerEntity> phaseDownCondition;

    private final RenderType renderType;
    private final float viewDistance;

    public PhasingPower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> blocks, boolean isBlacklist,
                        RenderType renderType, float viewDistance, Predicate<PlayerEntity> phaseDownCondition) {
        super(type, player);
        this.blocks = blocks;
        this.isBlacklist = isBlacklist;
        this.renderType = renderType;
        this.viewDistance = viewDistance;
        this.phaseDownCondition = phaseDownCondition;
    }

    public boolean doesApply(BlockPos pos) {
        return isBlacklist != blocks.test(new CachedBlockPosition(player.world, pos, true));
    }

    public boolean shouldPhaseDown(PlayerEntity playerEntity) {
        return phaseDownCondition == null ? playerEntity.isSneaking() : phaseDownCondition.test(playerEntity);
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public float getViewDistance() {
        return viewDistance;
    }

    public enum RenderType {
        BLINDNESS, REMOVE_BLOCKS
    }
}

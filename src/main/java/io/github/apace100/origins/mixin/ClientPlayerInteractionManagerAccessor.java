package io.github.apace100.origins.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getCurrentBreakingPos();

    @Accessor
    boolean getBreakingBlock();

    @Accessor
    GameMode getGameMode();
}

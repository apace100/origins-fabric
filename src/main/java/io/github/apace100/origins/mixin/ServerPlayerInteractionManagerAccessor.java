package io.github.apace100.origins.mixin;

import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerInteractionManager.class)
public interface ServerPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getMiningPos();

    @Accessor
    boolean getMining();

    @Accessor
    GameMode getGameMode();
}

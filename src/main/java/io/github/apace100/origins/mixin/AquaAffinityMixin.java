package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerEntity.class)
public abstract class AquaAffinityMixin extends LivingEntity implements Nameable, CommandOutput {

    protected AquaAffinityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    // AQUA_AFFINITY
    @ModifyConstant(method = "getBlockBreakingSpeed", constant = @Constant(ordinal = 0, floatValue = 5.0F))
    private float modifyWaterBlockBreakingSpeed(float in) {
        if(OriginsPowerTypes.AQUA_AFFINITY.isActive(this)) {
            return 1F;
        }
        return in;
    }

    // AQUA_AFFINITY
    @ModifyConstant(method = "getBlockBreakingSpeed", constant = @Constant(ordinal = 1, floatValue = 5.0F))
    private float modifyUngroundedBlockBreakingSpeed(float in) {
        if(this.isInsideWaterOrBubbleColumn() && OriginsPowerTypes.AQUA_AFFINITY.isActive(this)) {
            return 1F;
        }
        return in;
    }
}

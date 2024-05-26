package io.github.apace100.origins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.mixin.LivingEntityAccessor;
import io.github.apace100.origins.registry.ModDamageSources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;

public class WaterBreathingPower extends Power {

    public WaterBreathingPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public static boolean shouldDrown(LivingEntity entity) {
        return !entity.isSubmergedIn(FluidTags.WATER)
            && !entity.hasStatusEffect(StatusEffects.WATER_BREATHING)
            && !entity.hasStatusEffect(StatusEffects.CONDUIT_POWER);
    }

    public static void tick(LivingEntity entity) {

        if (!PowerHolderComponent.hasPower(entity, WaterBreathingPower.class)) {
            return;
        }

        LivingEntityAccessor entityAccess = (LivingEntityAccessor) entity;
        if (WaterBreathingPower.shouldDrown(entity)) {

            int landGain = entityAccess.callGetNextAirOnLand(0);
            int landLoss = entityAccess.callGetNextAirUnderwater(entity.getAir());

            if (!((EntityAccessor) entity).callIsBeingRainedOn()) {

                entity.setAir(landLoss - landGain);
                if (entity.getAir() != -20) {
                    return;
                }

                entity.setAir(0);
                entity.damage(ModDamageSources.getSource(entity.getDamageSources(), ModDamageSources.NO_WATER_FOR_GILLS), 2.0F);

                for (int i = 0; i < 8; ++i) {

                    double dx = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double dy = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double dz = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();

                    entity.getWorld().addParticle(ParticleTypes.BUBBLE, entity.getParticleX(0.5), entity.getEyeHeight(entity.getPose()), entity.getParticleZ(0.5), dx * 0.5, dy * 0.5 + 0.25, dz * 0.5);

                }

            }

            else {
                entity.setAir(entity.getAir() - landGain);
            }

        }

        else if (entity.getAir() < entity.getMaxAir()) {
            entity.setAir(entityAccess.callGetNextAirOnLand(entity.getAir()));
        }

    }

}

package io.github.apace100.origins.mixin;

import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.origins.power.OriginsPowerTypes;
import io.github.apace100.origins.registry.ModDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class WaterBreathingMixin {

    @Mixin(LivingEntity.class)
    public static abstract class CanBreatheInWater extends Entity {

        public CanBreatheInWater(EntityType<?> type, World world) {
            super(type, world);
        }

        @Inject(at = @At("HEAD"), method = "canBreatheInWater", cancellable = true)
        public void doWaterBreathing(CallbackInfoReturnable<Boolean> info) {
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                info.setReturnValue(true);
            }
        }
    }

    @Mixin(PlayerEntity.class)
    public static abstract class UpdateAir extends LivingEntity {

        protected UpdateAir(EntityType<? extends LivingEntity> entityType, World world) {
            super(entityType, world);
        }

        @Inject(at = @At("TAIL"), method = "tick")
        private void tick(CallbackInfo info) {
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                if(!this.isSubmergedIn(FluidTags.WATER) && !this.hasStatusEffect(StatusEffects.WATER_BREATHING) && !this.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
                    if(!((EntityAccessor) this).callIsBeingRainedOn()) {
                        int landGain = this.getNextAirOnLand(0);
                        this.setAir(this.getNextAirUnderwater(this.getAir()) - landGain);
                        if (this.getAir() == -20) {
                            this.setAir(0);

                            for(int i = 0; i < 8; ++i) {
                                double f = this.random.nextDouble() - this.random.nextDouble();
                                double g = this.random.nextDouble() - this.random.nextDouble();
                                double h = this.random.nextDouble() - this.random.nextDouble();
                                this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getParticleX(0.5), this.getEyeY() + this.random.nextGaussian() * 0.08D, this.getParticleZ(0.5), f * 0.5F, g * 0.5F + 0.25F, h * 0.5F);
                            }

                            this.damage(ModDamageSources.getSource(getDamageSources(), ModDamageSources.NO_WATER_FOR_GILLS), 2.0F);
                        }
                    } else {
                        int landGain = this.getNextAirOnLand(0);
                        this.setAir(this.getAir() - landGain);
                    }
                } else if(this.getAir() < this.getMaxAir()){
                    this.setAir(this.getNextAirOnLand(this.getAir()));
                }
            }
        }

        @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"), method = "updateTurtleHelmet")
        public boolean isSubmergedInProxy(PlayerEntity player, TagKey<Fluid> fluidTag) {
            boolean submerged = this.isSubmergedIn(fluidTag);
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                return !submerged;
            }
            return submerged;
        }
    }
}

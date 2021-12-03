package io.github.apace100.origins.integration;

import dev.micalobia.breathinglib.BreathingLib;
import dev.micalobia.breathinglib.data.BreathingInfo;
import dev.micalobia.breathinglib.event.BreathingCallback;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.Optional;

public class BreathingLibIntegration {
	public static void register() {
		BreathingCallback.EVENT.register(BreathingLibIntegration::reverseBreathingIfWaterBreathingPower);
	}

	static TypedActionResult<Optional<BreathingInfo>> reverseBreathingIfWaterBreathingPower(LivingEntity entity) {
//		// Don't touch breathing if we don't have the water breathing power
		if(!OriginsPowerTypes.WATER_BREATHING.isActive(entity))
			return TypedActionResult.pass(Optional.empty());
		ActionResult ret = BreathingLib.vanillaBreathing(entity);
		return switch(ret) {
			case SUCCESS -> ((EntityAccessor) entity).callIsBeingRainedOn() ?
					TypedActionResult.consume(Optional.empty()) :
					TypedActionResult.fail(Optional.empty());
			case FAIL -> TypedActionResult.success(Optional.empty());
			case PASS -> TypedActionResult.pass(Optional.empty());
			case CONSUME, CONSUME_PARTIAL -> TypedActionResult.consume(Optional.empty());
		};
	}
}

package io.github.apace100.origins.power.forge;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import top.theillusivec4.caelus.api.CaelusApi;

import java.util.UUID;

public class ElytraFlightPowerImpl {

	public static final EntityAttributeModifier FLIGHT_MODIFIER = new EntityAttributeModifier(UUID.fromString("29eb14ca-c803-4af6-81e2-86e9bf1d4857"), "Elytra modifier", 1.0f, EntityAttributeModifier.Operation.ADDITION);

	public static void enableFlight(PlayerEntity player) {
		if(player.getAttributes().hasAttribute(CaelusApi.ELYTRA_FLIGHT.get())) {
			player.getAttributeInstance(CaelusApi.ELYTRA_FLIGHT.get()).addTemporaryModifier(FLIGHT_MODIFIER);
		}
	}

	public static void disableFlight(PlayerEntity player) {
		if(player.getAttributes().hasAttribute(CaelusApi.ELYTRA_FLIGHT.get())) {
			player.getAttributeInstance(CaelusApi.ELYTRA_FLIGHT.get()).removeModifier(FLIGHT_MODIFIER);
		}
	}
}

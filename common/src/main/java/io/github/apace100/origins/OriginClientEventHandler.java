package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ParticlePower;
import io.github.apace100.origins.registry.ModComponentsArchitectury;
import me.shedaniel.architectury.event.events.TickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class OriginClientEventHandler {
	@Environment(EnvType.CLIENT)
	public static void register() {
		TickEvent.PLAYER_PRE.register(OriginClientEventHandler::tick);
	}

	@Environment(EnvType.CLIENT)
	private static void tick(PlayerEntity player) {
		if (player instanceof OtherClientPlayerEntity) {
			if(!player.isInvisibleTo(MinecraftClient.getInstance().player)) {
				OriginComponent component = ModComponentsArchitectury.getOriginComponent(player);
				List<ParticlePower> particlePowers = component.getPowers(ParticlePower.class);
				for (ParticlePower particlePower : particlePowers) {
					if(player.age % particlePower.getFrequency() == 0) {
						player.world.addParticle(particlePower.getParticle(), player.getParticleX(0.5), player.getRandomBodyY(), player.getParticleZ(0.5), 0, 0, 0);
					}
				}
			}
		}
	}
}

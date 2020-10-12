package io.github.apace100.origins;

import io.github.apace100.origins.power.factory.condition.PlayerConditionsServer;
import net.fabricmc.api.DedicatedServerModInitializer;

public class OriginsServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		PlayerConditionsServer.register();
	}
}

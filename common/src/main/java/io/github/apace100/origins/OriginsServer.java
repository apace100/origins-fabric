package io.github.apace100.origins;

import io.github.apace100.origins.power.factory.condition.PlayerConditionsServer;
import net.fabricmc.api.DedicatedServerModInitializer;

public class OriginsServer {

	public static void register() {
		PlayerConditionsServer.register();
	}
}

package io.github.apace100.origins;

import net.fabricmc.api.DedicatedServerModInitializer;

public class OriginsServerFabric implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		OriginsServer.register();
	}
}

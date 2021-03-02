package io.github.apace100.origins;

import net.fabricmc.api.ClientModInitializer;

public class OriginsClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OriginsClient.register();
	}
}

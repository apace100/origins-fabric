package io.github.apace100.origins.origin;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class OriginManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
	
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public OriginManager() {
		super(GSON, "origins");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
		OriginRegistry.reset();
		loader.forEach((id, jo) -> {
			try {
				Origin origin = Origin.fromJson(jo.getAsJsonObject());
				if(!OriginRegistry.contains(id)) {
					OriginRegistry.register(id, origin);
				}
			} catch(Exception e) {
				Origins.LOGGER.error("There was a problem reading Origin file " + id.toString() + " (skipping): " + e.getMessage());
			}
		});

	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Origins.MODID, "origins");
	}
}

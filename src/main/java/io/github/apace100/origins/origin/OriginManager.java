package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

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
				} else {
					if(OriginRegistry.get(id).getLoadingPriority() < origin.getLoadingPriority()) {
						OriginRegistry.update(id, origin);
					}
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

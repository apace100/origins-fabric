package io.github.apace100.origins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.calio.util.IdentifierAlias;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.command.OriginCommand;
import io.github.apace100.origins.condition.factory.entity.OriginsEntityConditionTypes;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsC2S;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.power.type.OriginsPowerTypes;
import io.github.apace100.origins.registry.*;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import io.github.apace100.origins.util.OriginLootCondition;
import io.github.apace100.origins.util.OriginsConfigSerializer;
import io.github.apace100.origins.util.OriginsJsonConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Origins implements ModInitializer {

	public static final String MODID = "origins";
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);

	public static String VERSION = "";
	public static int[] SEMVER;

	public static ServerConfig config;
	private static ConfigSerializer<ServerConfig> configSerializer;

	@Override
	public void onInitialize() {
		FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
			VERSION = modContainer.getMetadata().getVersion().getFriendlyString();
			if(VERSION.contains("+")) {
				VERSION = VERSION.split("\\+")[0];
			}
			if(VERSION.contains("-")) {
				VERSION = VERSION.split("-")[0];
			}
			String[] splitVersion = VERSION.split("\\.");
			SEMVER = new int[splitVersion.length];
			for(int i = 0; i < SEMVER.length; i++) {
				SEMVER[i] = Integer.parseInt(splitVersion[i]);
			}
		});
		LOGGER.info("Origins " + VERSION + " is initializing. Have fun!");

		AutoConfig.register(ServerConfig.class,
			(definition, configClass) -> {
				configSerializer = new OriginsJsonConfigSerializer<>(definition, configClass,
					new OriginsConfigSerializer<>(definition, configClass));
				return configSerializer;
			});
		config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();

		IdentifierAlias.GLOBAL.addNamespaceAlias(MODID, "apoli");

		OriginsPowerTypes.register();
		OriginsEntityConditionTypes.register();

		ModBlocks.register();
		ModItems.register();
		ModTags.register();

		ModPackets.register();
		ModPacketsC2S.register();

		ModEntities.register();
		ModLoot.registerLootTables();

		ModComponents.register();
		ModDataComponentTypes.register();

		Origin.init();
		BadgeManager.init();

		OriginManager originManager = new OriginManager();
		OriginLayerManager originLayerManager = new OriginLayerManager();
		IdentifiableResourceReloadListener badgeManager = BadgeManager.REGISTRY.getLoader();

		PowerManager.DEPENDENCIES.add(badgeManager.getFabricId());

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(originManager);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(originLayerManager);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(badgeManager);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> OriginCommand.register(dispatcher.getRoot()));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(ModItems.ORB_OF_ORIGIN));

		Criteria.register(ChoseOriginCriterion.ID.toString(), ChoseOriginCriterion.INSTANCE);
		Registry.register(Registries.LOOT_CONDITION_TYPE, identifier("origin"), OriginLootCondition.TYPE);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ModComponents.ORIGIN.get(handler.player).selectingOrigin(false));

	}

	public static void serializeConfig() {
		try {
			configSerializer.serialize(config);
		} catch (ConfigSerializer.SerializationException e) {
			Origins.LOGGER.error("Failed serialization of config file: " + e.getMessage());
		}
	}

	public static Identifier identifier(String path) {
		return Identifier.of(Origins.MODID, path);
	}

	@Config(name = Origins.MODID + "_server")
	public static class ServerConfig implements ConfigData {

		public boolean performVersionCheck = true;

		public JsonObject origins = new JsonObject();

		public boolean isOriginDisabled(Identifier originId) {
			String idString = originId.toString();
			if(!origins.has(idString)) {
				return false;
			}
			JsonElement element = origins.get(idString);
			if(element instanceof JsonObject jsonObject) {
				return !JsonHelper.getBoolean(jsonObject, "enabled", true);
			}
			return false;
		}

		public boolean isPowerDisabled(Identifier originId, Identifier powerId) {
			String originIdString = originId.toString();
			if(!origins.has(originIdString)) {
				return false;
			}
			String powerIdString = powerId.toString();
			JsonElement element = origins.get(originIdString);
			if(element instanceof JsonObject jsonObject) {
				return !JsonHelper.getBoolean(jsonObject, powerIdString, true);
			}
			return false;
		}

		public boolean addToConfig(Origin origin) {
			boolean changed = false;
			String originIdString = origin.getId().toString();
			JsonObject originObj;
			if(!origins.has(originIdString) || !(origins.get(originIdString) instanceof JsonObject)) {
				originObj = new JsonObject();
				origins.add(originIdString, originObj);
				changed = true;
			} else {
				originObj = (JsonObject) origins.get(originIdString);
			}
			if(!originObj.has("enabled") || !(originObj.get("enabled") instanceof JsonPrimitive)) {
				originObj.addProperty("enabled", Boolean.TRUE);
				changed = true;
			}
			for(Power power : origin.getPowers()) {
				String powerIdString = power.getId().toString();
				if(!originObj.has(powerIdString) || !(originObj.get(powerIdString) instanceof JsonPrimitive)) {
					originObj.addProperty(powerIdString, Boolean.TRUE);
					changed = true;
				}
			}
			return changed;
		}
	}
}

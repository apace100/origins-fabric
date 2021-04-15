package io.github.apace100.origins;

import io.github.apace100.origins.command.*;
import io.github.apace100.origins.networking.ModPacketsC2S;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.power.factory.PowerFactories;
import io.github.apace100.origins.power.factory.action.BlockActions;
import io.github.apace100.origins.power.factory.action.EntityActions;
import io.github.apace100.origins.power.factory.action.ItemActions;
import io.github.apace100.origins.power.factory.condition.*;
import io.github.apace100.origins.registry.*;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import io.github.apace100.origins.util.GainedPowerCriterion;
import io.github.apace100.origins.util.OriginsConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.registry.CriteriaTriggersRegistry;
import me.shedaniel.architectury.registry.ReloadListeners;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Origins {

	public static final String MODID = "origins";
	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);
	public static ServerConfig config;

	public static void register() {
		LOGGER.info("Origins " + VERSION + " is initializing. Have fun!");
		AutoConfig.register(ServerConfig.class, OriginsConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();

		ModBlocks.register();
		ModItems.register();
		ModTags.register();
		ModPacketsC2S.register();
		ModEnchantments.register();
		ModEntities.register();
		ModLoot.registerLootTables();
		ModRecipes.register();
		PowerFactories.register();
		EntityConditions.register();
		ItemConditions.register();
		BlockConditions.register();
		DamageConditions.register();
		FluidConditions.register();
		BiomeConditions.register();
		EntityActions.register();
		ItemActions.register();
		BlockActions.register();
		Origin.init();
		OriginEventHandler.register();
		CommandRegistrationEvent.EVENT.register((dispatcher, dedicated) -> {
			OriginCommand.register(dispatcher);
			ResourceCommand.register(dispatcher);
		});
		CriteriaTriggersRegistry.register(ChoseOriginCriterion.INSTANCE);
		CriteriaTriggersRegistry.register(GainedPowerCriterion.INSTANCE);
		ArgumentTypes.register("origins:origin", OriginArgument.class, new ConstantArgumentSerializer<>(OriginArgument::origin));
		ArgumentTypes.register("origins:layer", LayerArgument.class, new ConstantArgumentSerializer<>(LayerArgument::layer));
		ArgumentTypes.register("origins:power", PowerArgument.class, new ConstantArgumentSerializer<>(PowerArgument::power));
		ArgumentTypes.register("origins:power_operation", PowerOperation.class, new ConstantArgumentSerializer<>(PowerOperation::operation));
		ReloadListeners.registerReloadListener(ResourceType.SERVER_DATA, new PowerTypes());
		ReloadListeners.registerReloadListener(ResourceType.SERVER_DATA, new OriginManager());
		ReloadListeners.registerReloadListener(ResourceType.SERVER_DATA, new OriginLayers());
	}

	public static Identifier identifier(String path) {
		return new Identifier(Origins.MODID, path);
	}


	@Config(name = Origins.MODID + "_server")
	public static class ServerConfig implements ConfigData {
		public boolean performVersionCheck = true;
	}
}

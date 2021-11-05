package io.github.apace100.origins;

import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.mixin.CriteriaRegistryInvoker;
import io.github.apace100.calio.util.OrderedResourceListeners;
import io.github.apace100.origins.command.LayerArgumentType;
import io.github.apace100.origins.command.OriginArgumentType;
import io.github.apace100.origins.command.OriginCommand;
import io.github.apace100.origins.networking.ModPacketsC2S;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.power.OriginsEntityConditions;
import io.github.apace100.origins.power.OriginsPowerTypes;
import io.github.apace100.origins.registry.*;
import io.github.apace100.origins.screen.BadgeManager;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import io.github.apace100.origins.util.OriginsConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Origins implements ModInitializer {

	public static final String MODID = "origins";
	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);

	public static ServerConfig config;

	public static BadgeManager badgeManager;

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
		AutoConfig.register(ServerConfig.class, OriginsConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();

		NamespaceAlias.addAlias(MODID, "apoli");

		OriginsPowerTypes.register();
		OriginsEntityConditions.register();

		ModBlocks.register();
		ModItems.register();
		ModTags.register();
		ModPacketsC2S.register();
		ModEnchantments.register();
		ModEntities.register();
		ModLoot.registerLootTables();
		Origin.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			OriginCommand.register(dispatcher);
		});
		CriteriaRegistryInvoker.callRegister(ChoseOriginCriterion.INSTANCE);
		ArgumentTypes.register("origins:origin", OriginArgumentType.class, new ConstantArgumentSerializer<>(OriginArgumentType::origin));
		ArgumentTypes.register("origins:layer", LayerArgumentType.class, new ConstantArgumentSerializer<>(LayerArgumentType::layer));

		OrderedResourceListeners.register(new OriginManager()).after(new Identifier("apoli", "powers")).complete();
		OrderedResourceListeners.register(new OriginLayers()).after(new Identifier(Origins.MODID, "origins")).complete();

		badgeManager = new BadgeManager();
	}

	public static Identifier identifier(String path) {
		return new Identifier(Origins.MODID, path);
	}

	@Config(name = Origins.MODID + "_server")
	public static class ServerConfig implements ConfigData {

		public boolean performVersionCheck = true;
	}
}

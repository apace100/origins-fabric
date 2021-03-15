package io.github.apace100.origins;

import io.github.apace100.origins.command.*;
import io.github.apace100.origins.mixin.CriteriaRegistryInvoker;
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
import io.github.apace100.origins.util.ElytraPowerFallFlying;
import io.github.apace100.origins.util.GainedPowerCriterion;
import io.github.apace100.origins.util.OriginsConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.adriantodt.fallflyinglib.FallFlyingLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Origins implements ModInitializer {

	public static final String MODID = "origins";
	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);

	public static ServerConfig config;

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
		FallFlyingLib.registerAccessor(ElytraPowerFallFlying::new);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			OriginCommand.register(dispatcher);
			ResourceCommand.register(dispatcher);
		});
		CriteriaRegistryInvoker.callRegister(ChoseOriginCriterion.INSTANCE);
		CriteriaRegistryInvoker.callRegister(GainedPowerCriterion.INSTANCE);
		ArgumentTypes.register("origins:origin", OriginArgument.class, new ConstantArgumentSerializer<>(OriginArgument::origin));
		ArgumentTypes.register("origins:layer", LayerArgument.class, new ConstantArgumentSerializer<>(LayerArgument::layer));
		ArgumentTypes.register("origins:power", PowerArgument.class, new ConstantArgumentSerializer<>(PowerArgument::power));
		ArgumentTypes.register("origins:power_operation", PowerOperation.class, new ConstantArgumentSerializer<>(PowerOperation::operation));
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PowerTypes());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new OriginManager());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new OriginLayers());

	}

	public static Identifier identifier(String path) {
		return new Identifier(Origins.MODID, path);
	}

	@Config(name = Origins.MODID + "_server")
	public static class ServerConfig implements ConfigData {

		public boolean performVersionCheck = true;
	}
}

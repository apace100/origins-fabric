package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.Active;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class ModPacketsC2S {

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.CHOOSE_ORIGIN, ((packetContext, packetByteBuf) -> {
            String originId = packetByteBuf.readString(32767);
            String layerId = packetByteBuf.readString(32767);
            packetContext.getTaskQueue().execute(() -> {
                ServerPlayerEntity player = (ServerPlayerEntity)packetContext.getPlayer();
                OriginComponent component = ModComponents.ORIGIN.get(player);
                OriginLayer layer = OriginLayers.getLayer(Identifier.tryParse(layerId));
                if(!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                    Identifier id = Identifier.tryParse(originId);
                    if(id != null) {
                        Origin origin = OriginRegistry.get(id);
                        if(origin.isChoosable() && layer.contains(origin, player)) {
                            boolean hadOriginBefore = component.hadOriginBefore();
                            boolean hadAllOrigins = component.hasAllOrigins();
                            component.setOrigin(layer, origin);
                            component.sync();
                            if(component.hasAllOrigins() && !hadAllOrigins) {
                                component.getOrigins().values().forEach(o -> {
                                    o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(hadOriginBefore));
                                });
                            }
                            Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " chose Origin: " + originId + ", for layer: " + layerId);
                        } else {
                            Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " tried to choose unchoosable Origin for layer " + layerId + ": " + originId + ".");
                            component.setOrigin(layer, Origin.EMPTY);
                        }
                        component.sync();
                    } else {
                        Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " chose unknown origin: " + originId);
                    }
                } else {
                    Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " tried to choose origin for layer " + layerId + " while having one already.");
                }
            });
        }));
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.CHOOSE_RANDOM_ORIGIN, ((packetContext, packetByteBuf) -> {
            String layerId = packetByteBuf.readString(32767);
            packetContext.getTaskQueue().execute(() -> {
                ServerPlayerEntity player = (ServerPlayerEntity)packetContext.getPlayer();
                OriginComponent component = ModComponents.ORIGIN.get(player);
                OriginLayer layer = OriginLayers.getLayer(Identifier.tryParse(layerId));
                if(!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                    List<Identifier> randomOrigins = layer.getRandomOrigins(player);
                    if(layer.isRandomAllowed() && randomOrigins.size() > 0) {
                        Identifier randomOrigin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
                        Origin origin = OriginRegistry.get(randomOrigin);
                        boolean hadOriginBefore = component.hadOriginBefore();
                        boolean hadAllOrigins = component.hasAllOrigins();
                        component.setOrigin(layer, origin);
                        component.sync();
                        if(component.hasAllOrigins() && !hadAllOrigins) {
                            component.getOrigins().values().forEach(o -> {
                                o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(hadOriginBefore));
                            });
                        }
                        Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " was randomly assigned the following Origin: " + randomOrigin + ", for layer: " + layerId);
                    } else {
                        Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " tried to choose a random Origin for layer " + layerId + ", which is not allowed!");
                        component.setOrigin(layer, Origin.EMPTY);
                    }
                    component.sync();
                } else {
                    Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " tried to choose origin for layer " + layerId + " while having one already.");
                }
            });
        }));
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.USE_ACTIVE_POWER, ((packetContext, packetByteBuf) -> {
            Active.KeyType keyType = Active.KeyType.values()[packetByteBuf.readInt()];
            packetContext.getTaskQueue().execute(() -> {
                OriginComponent component = ModComponents.ORIGIN.get(packetContext.getPlayer());
                if(component.hasAllOrigins()) {
                    component.getPowers().stream().filter(p -> p instanceof Active && ((Active)p).getKey() == keyType).forEach(p -> ((Active)p).onUse());
                }
            });
        }));
    }
}

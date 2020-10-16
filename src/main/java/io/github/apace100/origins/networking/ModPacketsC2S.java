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
                                    Origins.LOGGER.info("Calling Powers onChosen for " + o.getIdentifier());
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
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.USE_ACTIVE_POWER, ((packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                OriginComponent component = ModComponents.ORIGIN.get(packetContext.getPlayer());
                if(component.hasAllOrigins()) {
                    component.getPowers().stream().filter(p -> p instanceof Active).forEach(p -> ((Active)p).onUse());
                }
            });
        }));
    }
}

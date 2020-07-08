package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.Active;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModPacketsC2S {

    public static void register() {
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.CHOOSE_ORIGIN, ((packetContext, packetByteBuf) -> {
            String originId = packetByteBuf.readString(32767);
            packetContext.getTaskQueue().execute(() -> {
                ServerPlayerEntity player = (ServerPlayerEntity)packetContext.getPlayer();
                OriginComponent component = ModComponents.ORIGIN.get(player);
                if(!component.hasOrigin()) {
                    Identifier id = Identifier.tryParse(originId);
                    if(id != null) {
                        Origin origin = OriginRegistry.get(id);
                        if(origin.isChoosable()) {
                            component.setOrigin(origin);
                            component.sync();
                            component.getPowers().forEach(Power::onChosen);
                            Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " chose Origin: " + originId);
                        } else {
                            Origins.LOGGER.info("Player " + player.getDisplayName().asString() + " tried to choose unchoosable Origin: " + originId + ", setting them to Human.");
                            component.setOrigin(Origin.HUMAN);
                            component.sync();
                        }
                    } else {
                        Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " chose unknown origin: " + originId);
                    }
                } else {
                    Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " tried to choose origin while having one already.");
                }
            });
        }));
        ServerSidePacketRegistry.INSTANCE.register(ModPackets.USE_ACTIVE_POWER, ((packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                OriginComponent component = ModComponents.ORIGIN.get(packetContext.getPlayer());
                if(component.hasOrigin()) {
                    component.getPowers().stream().filter(p -> p instanceof Active).forEach(p -> ((Active)p).onUse());
                }
            });
        }));
    }
}

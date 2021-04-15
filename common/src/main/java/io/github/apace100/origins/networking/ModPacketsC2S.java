package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class ModPacketsC2S {

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ModPackets.CHOOSE_ORIGIN, ModPacketsC2S::chooseOrigin);
        NetworkManager.registerReceiver(NetworkManager.c2s(), ModPackets.CHOOSE_RANDOM_ORIGIN, ModPacketsC2S::chooseRandomOrigin);
        NetworkManager.registerReceiver(NetworkManager.c2s(), ModPackets.USE_ACTIVE_POWERS, ModPacketsC2S::useActivePowers);
        NetworkManager.registerReceiver(NetworkManager.c2s(), ModPackets.PLAYER_LANDED, ModPacketsC2S::playerLanded);
        registerPlatformSpecificPackets();
    }

    @ExpectPlatform
    private static void registerPlatformSpecificPackets() {
        throw new AssertionError();
    }

    private static void playerLanded(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        context.queue(() -> {
            OriginComponent.getPowers(playerEntity, ActionOnLandPower.class).forEach(ActionOnLandPower::executeAction);
        });
    }

    private static void useActivePowers(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        int count = packetByteBuf.readInt();
        Identifier[] powerIds = new Identifier[count];
        for(int i = 0; i < count; i++) {
            powerIds[i] = packetByteBuf.readIdentifier();
        }
        context.queue(() -> {
            OriginComponent component = ModComponents.getOriginComponent(playerEntity);
            for(Identifier id : powerIds) {
                PowerType<?> type = PowerTypeRegistry.get(id);
                Power power = component.getPower(type);
                if(power instanceof Active) {
                    ((Active)power).onUse();
                }
            }
        });
    }

    private static void chooseOrigin(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        String originId = packetByteBuf.readString(32767);
        String layerId = packetByteBuf.readString(32767);
        context.queue(() -> {
            OriginComponent component = ModComponents.getOriginComponent(playerEntity);
            OriginLayer layer = OriginLayers.getLayer(Identifier.tryParse(layerId));
            if(!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                Identifier id = Identifier.tryParse(originId);
                if(id != null) {
                    Origin origin = OriginRegistry.get(id);
                    if(origin.isChoosable() && layer.contains(origin, playerEntity)) {
                        boolean hadOriginBefore = component.hadOriginBefore();
                        boolean hadAllOrigins = component.hasAllOrigins();
                        component.setOrigin(layer, origin);
                        component.checkAutoChoosingLayers(playerEntity, false);
                        component.sync();
                        if(component.hasAllOrigins() && !hadAllOrigins) {
                            component.getOrigins().values().forEach(o -> o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(hadOriginBefore)));
                        }
                        Origins.LOGGER.info("Player " + playerEntity.getDisplayName().asString() + " chose Origin: " + originId + ", for layer: " + layerId);
                    } else {
                        Origins.LOGGER.info("Player " + playerEntity.getDisplayName().asString() + " tried to choose unchoosable Origin for layer " + layerId + ": " + originId + ".");
                        component.setOrigin(layer, Origin.EMPTY);
                    }
                    confirmOrigin((ServerPlayerEntity) playerEntity, layer, component.getOrigin(layer));
                    component.sync();
                } else {
                    Origins.LOGGER.warn("Player " + playerEntity.getDisplayName().asString() + " chose unknown origin: " + originId);
                }
            } else {
                Origins.LOGGER.warn("Player " + playerEntity.getDisplayName().asString() + " tried to choose origin for layer " + layerId + " while having one already.");
            }
        });
    }

    private static void chooseRandomOrigin(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        String layerId = packetByteBuf.readString(32767);
        context.queue(() -> {
            OriginComponent component = ModComponents.getOriginComponent(playerEntity);
            OriginLayer layer = OriginLayers.getLayer(Identifier.tryParse(layerId));
            if(!component.hasAllOrigins() && !component.hasOrigin(layer)) {
                List<Identifier> randomOrigins = layer.getRandomOrigins(playerEntity);
                if(layer.isRandomAllowed() && randomOrigins.size() > 0) {
                    Identifier randomOrigin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
                    Origin origin = OriginRegistry.get(randomOrigin);
                    boolean hadOriginBefore = component.hadOriginBefore();
                    boolean hadAllOrigins = component.hasAllOrigins();
                    component.setOrigin(layer, origin);
                    component.checkAutoChoosingLayers(playerEntity, false);
                    component.sync();
                    if(component.hasAllOrigins() && !hadAllOrigins) {
                        component.getOrigins().values().forEach(o -> o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(hadOriginBefore)));
                    }
                    Origins.LOGGER.info("Player " + playerEntity.getDisplayName().asString() + " was randomly assigned the following Origin: " + randomOrigin + ", for layer: " + layerId);
                } else {
                    Origins.LOGGER.info("Player " + playerEntity.getDisplayName().asString() + " tried to choose a random Origin for layer " + layerId + ", which is not allowed!");
                    component.setOrigin(layer, Origin.EMPTY);
                }
                confirmOrigin((ServerPlayerEntity) playerEntity, layer, component.getOrigin(layer));
                component.sync();
            } else {
                Origins.LOGGER.warn("Player " + playerEntity.getDisplayName().asString() + " tried to choose origin for layer " + layerId + " while having one already.");
            }
        });
    }

    private static void confirmOrigin(ServerPlayerEntity player, OriginLayer layer, Origin origin) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(layer.getIdentifier());
        buf.writeIdentifier(origin.getIdentifier());
        NetworkManager.sendToPlayer(player, ModPackets.CONFIRM_ORIGIN, buf);
    }
}

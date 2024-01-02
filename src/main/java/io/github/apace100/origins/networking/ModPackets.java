package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.s2c.*;
import net.minecraft.util.Identifier;

public class ModPackets {

    public static final Identifier HANDSHAKE = Origins.identifier("handshake");

    public static final Identifier OPEN_ORIGIN_SCREEN = OpenChooseOriginScreenS2CPacket.TYPE.getId();
    public static final Identifier CHOOSE_ORIGIN = new Identifier(Origins.MODID, "choose_origin");
    public static final Identifier USE_ACTIVE_POWERS = new Identifier(Origins.MODID, "use_active_powers");
    public static final Identifier ORIGIN_LIST = SyncOriginRegistryS2CPacket.TYPE.getId();
    public static final Identifier LAYER_LIST = SyncOriginLayerRegistryS2CPacket.TYPE.getId();
    public static final Identifier POWER_LIST = new Identifier(Origins.MODID, "power_list");
    public static final Identifier CHOOSE_RANDOM_ORIGIN = new Identifier(Origins.MODID, "choose_random_origin");
    public static final Identifier CONFIRM_ORIGIN = ConfirmOriginS2CPacket.TYPE.getId();
    public static final Identifier PLAYER_LANDED = Origins.identifier("player_landed");
    public static final Identifier BADGE_LIST = Origins.identifier("badge_list");

}

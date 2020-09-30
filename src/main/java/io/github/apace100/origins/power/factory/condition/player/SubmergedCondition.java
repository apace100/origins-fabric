package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.SerializationHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SubmergedCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "submerged_in");

    private final Tag<Fluid> fluidTag;

    public SubmergedCondition(String fluidTag) {
        this(SerializationHelper.getFluidTagFromId(Identifier.tryParse(fluidTag)));
    }

    public SubmergedCondition(Tag<Fluid> fluidTag) {
        this.fluidTag = fluidTag;
        if(this.fluidTag == null) {
            Origins.LOGGER.warn("SubmergedCondition was initialized with unknown fluid tag: " + fluidTag);
        }
    }

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        if(this.fluidTag == null) {
            return false;
        }
        return playerEntity.isSubmergedIn(fluidTag);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<SubmergedCondition> {

        @Override
        public void write(SubmergedCondition condition, PacketByteBuf buf) {
            buf.writeIdentifier(ServerTagManagerHolder.getTagManager().getFluids().getTagId(condition.fluidTag));
        }

        @Override
        public SubmergedCondition read(PacketByteBuf buf) {
            Identifier id = buf.readIdentifier();
            Tag<Fluid> tag = SerializationHelper.getFluidTagFromId(id);
            return new SubmergedCondition(tag);
        }

        @Override
        public SubmergedCondition read(JsonObject json) {
            String fluidTag = JsonHelper.getString(json, "fluid", "");
            if(fluidTag.isEmpty()) {
                throw new JsonSyntaxException("SubmergedCondition json requires \"fluid\" tag identifier.");
            }
            return new SubmergedCondition(fluidTag);
        }
    }
}

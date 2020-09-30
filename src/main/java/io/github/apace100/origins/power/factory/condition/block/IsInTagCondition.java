package io.github.apace100.origins.power.factory.condition.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.SerializationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class IsInTagCondition extends BlockCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_in_tag");

    private Identifier tagId;
    private final Tag<Block> blockTag;

    public IsInTagCondition(String blockTagId) {
        this(Identifier.tryParse(blockTagId));
    }

    public IsInTagCondition(Identifier id) {
        tagId = id;
        blockTag = SerializationHelper.getBlockTagFromId(tagId);
    }

    @Override
    protected boolean isFulfilled(CachedBlockPosition cachedBlockPosition) {
        if(blockTag == null) {
            Origins.LOGGER.info("Block tag " + tagId + " is null");
            return false;
        }
        return cachedBlockPosition.getBlockState().isIn(blockTag);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends BlockCondition.Serializer<IsInTagCondition> {

        @Override
        public void write(IsInTagCondition condition, PacketByteBuf buf) {
            buf.writeIdentifier(condition.tagId);
        }

        @Override
        public IsInTagCondition read(PacketByteBuf buf) {
            Identifier id = buf.readIdentifier();
            return new IsInTagCondition(id);
        }

        @Override
        public IsInTagCondition read(JsonObject json) {
            String blockTag = JsonHelper.getString(json, "tag", "");
            if(blockTag.isEmpty()) {
                throw new JsonSyntaxException("IsInTagCondition json requires \"tag\" identifier.");
            }
            return new IsInTagCondition(blockTag);
        }
    }
}

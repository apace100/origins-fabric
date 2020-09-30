package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class BlockCollisionCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "block_collision");

    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public BlockCollisionCondition(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public boolean isFulfilled(PlayerEntity p) {
        return p.world.getBlockCollisions(p,
            p.getBoundingBox().offset(
                offsetX * p.getBoundingBox().getXLength(),
                offsetY * p.getBoundingBox().getYLength(),
                offsetZ * p.getBoundingBox().getZLength())
        ).findAny().isPresent();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<BlockCollisionCondition> {

        @Override
        public void write(BlockCollisionCondition condition, PacketByteBuf buf) {
            buf.writeFloat(condition.offsetX);
            buf.writeFloat(condition.offsetY);
            buf.writeFloat(condition.offsetZ);
        }

        @Override
        public BlockCollisionCondition read(PacketByteBuf buf) {
            return new BlockCollisionCondition(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public BlockCollisionCondition read(JsonObject json) {
            if(!json.has("offset_x") || !json.get("offset_x").isJsonPrimitive()) {
                throw new JsonSyntaxException("BlockCollisionCondition json requires \"offset_x\" float.");
            }
            if(!json.has("offset_y") || !json.get("offset_y").isJsonPrimitive()) {
                throw new JsonSyntaxException("BlockCollisionCondition json requires \"offset_y\" float.");
            }
            if(!json.has("offset_z") || !json.get("offset_z").isJsonPrimitive()) {
                throw new JsonSyntaxException("BlockCollisionCondition json requires \"offset_x\" float.");
            }
            float offsetX = JsonHelper.getFloat(json, "offset_x");
            float offsetY = JsonHelper.getFloat(json, "offset_y");
            float offsetZ = JsonHelper.getFloat(json, "offset_z");
            return new BlockCollisionCondition(offsetX, offsetY, offsetZ);
        }
    }
}

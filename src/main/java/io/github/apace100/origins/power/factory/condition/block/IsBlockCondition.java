package io.github.apace100.origins.power.factory.condition.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class IsBlockCondition extends BlockCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_block");

    private Identifier id;
    private Block block;

    public IsBlockCondition(String blockId) {
        this(Identifier.tryParse(blockId));
    }

    public IsBlockCondition(Identifier id) {
        this.id = id;
        Optional<Block> blockOptional = Registry.BLOCK.getOrEmpty(id);
        if(!blockOptional.isPresent()) {
            throw new JsonSyntaxException("IsBlockCondition had invalid block: block with id \"" + id + "\" was not registered.");
        }
        this.block = blockOptional.get();
    }

    @Override
    protected boolean isFulfilled(CachedBlockPosition cachedBlockPosition) {
        return cachedBlockPosition.getBlockState().isOf(block);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends BlockCondition.Serializer<IsBlockCondition> {

        @Override
        public void write(IsBlockCondition condition, PacketByteBuf buf) {
            buf.writeIdentifier(condition.id);
        }

        @Override
        public IsBlockCondition read(PacketByteBuf buf) {
            Identifier id = buf.readIdentifier();
            return new IsBlockCondition(id);
        }

        @Override
        public IsBlockCondition read(JsonObject json) {
            String block = JsonHelper.getString(json, "block", "");
            if(block.isEmpty()) {
                throw new JsonSyntaxException("IsInTagCondition json requires \"block\" identifier.");
            }
            return new IsBlockCondition(block);
        }
    }
}

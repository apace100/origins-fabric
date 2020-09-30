package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EntityPredicateCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "predicate");

    private EntityPredicate predicate;

    public EntityPredicateCondition(EntityPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        if(predicate == null) {
            return false;
        }
        if(playerEntity instanceof ServerPlayerEntity) {
            return predicate.test((ServerPlayerEntity)playerEntity, playerEntity);
        }
        return false;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<EntityPredicateCondition> {

        @Override
        public void write(EntityPredicateCondition condition, PacketByteBuf buf) {}

        @Environment(EnvType.CLIENT)
        @Override
        public EntityPredicateCondition read(PacketByteBuf buf) {
            return new EntityPredicateCondition(null);
        }

        @Override
        public EntityPredicateCondition read(JsonObject json) {
            if(!json.has("predicate")) {
                throw new JsonSyntaxException("EntityPredicateCondition requires \"predicate\" object.");
            }
            EntityPredicate predicate = EntityPredicate.fromJson(json.get("predicate"));
            return new EntityPredicateCondition(predicate);
        }
    }
}

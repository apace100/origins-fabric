package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ChoseOriginCriterion extends AbstractCriterion<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    private static final Identifier ID = new Identifier(Origins.MODID, "chose_origin");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Identifier id = Identifier.tryParse(JsonHelper.getString(obj, "origin"));
        return new Conditions(playerPredicate, id);
    }

    public void trigger(ServerPlayerEntity player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Identifier originId;

        public Conditions(LootContextPredicate player, Identifier originId) {
            super(ChoseOriginCriterion.ID, player);
            this.originId = originId;
        }

        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("origin", new JsonPrimitive(originId.toString()));
            return jsonObject;
        }
    }
}

package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class ChoseOriginCriterion extends AbstractCriterion<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();
    public static final Identifier ID = new Identifier(Origins.MODID, "chose_origin");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Identifier id = new Identifier(JsonHelper.getString(obj, "origin"));
        return new Conditions(predicate, id);
    }

    public void trigger(ServerPlayerEntity player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    public Identifier getId() {
        return ID;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Conditions extends AbstractCriterionConditions {

        private final Identifier originId;

        public Conditions(Optional<LootContextPredicate> player, Identifier originId) {
            super(player);
            this.originId = originId;
        }

        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

        @Override
        public JsonObject toJson() {

            JsonObject jsonObject = super.toJson();
            jsonObject.addProperty("origin", originId.toString());

            return jsonObject;

        }

    }

}

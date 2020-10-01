package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class GainedPowerCriterion extends AbstractCriterion<GainedPowerCriterion.Conditions> {

    public static GainedPowerCriterion INSTANCE = new GainedPowerCriterion();

    private static final Identifier ID = new Identifier(Origins.MODID, "gained_power");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Identifier id = Identifier.tryParse(JsonHelper.getString(obj, "power"));
        return new Conditions(playerPredicate, id);
    }

    public void trigger(ServerPlayerEntity player, PowerType type) {
        this.test(player, (conditions -> conditions.matches(type)));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Identifier powerId;

        public Conditions(EntityPredicate.Extended player, Identifier powerId) {
            super(GainedPowerCriterion.ID, player);
            this.powerId = powerId;
        }

        public boolean matches(PowerType powerType) {
            return powerType.getIdentifier().equals(powerId);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("power", new JsonPrimitive(powerId.toString()));
            return jsonObject;
        }
    }
}

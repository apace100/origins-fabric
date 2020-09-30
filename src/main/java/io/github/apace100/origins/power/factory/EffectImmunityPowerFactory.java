package io.github.apace100.origins.power.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.EffectImmunityPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;

public class EffectImmunityPowerFactory extends PowerFactory<EffectImmunityPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "effect_immunity");

    private final HashSet<StatusEffect> statusEffects;

    public EffectImmunityPowerFactory(HashSet<StatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    @Override
    public EffectImmunityPower apply(PowerType<EffectImmunityPower> powerType, PlayerEntity playerEntity) {
        EffectImmunityPower power = new EffectImmunityPower(powerType, playerEntity);
        for(StatusEffect se : statusEffects) {
            power.addEffect(se);
        }
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<EffectImmunityPowerFactory> {

        @Override
        public void write(EffectImmunityPowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.statusEffects.size());
            for(StatusEffect se : factory.statusEffects) {
                buf.writeIdentifier(Registry.STATUS_EFFECT.getId(se));
            }
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public EffectImmunityPowerFactory read(PacketByteBuf buf) {
            int count = buf.readInt();
            HashSet<StatusEffect> effects = new HashSet<>(count);
            for(int i = 0; i < count; i++) {
                effects.add(Registry.STATUS_EFFECT.get(buf.readIdentifier()));
            }
            EffectImmunityPowerFactory factory = new EffectImmunityPowerFactory(effects);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public EffectImmunityPowerFactory read(JsonObject json) {
            HashSet<StatusEffect> effects = new HashSet<>();
            if(json.has("effect")) {
                String idString = JsonHelper.getString(json, "effect", "");
                if(!idString.isEmpty()) {
                    Identifier id = Identifier.tryParse(idString);
                    StatusEffect effect = Registry.STATUS_EFFECT.get(id);
                    if(effect == null) {
                        throw new JsonSyntaxException("EffectImmunityPower could not find effect with id '" + id + "'.");
                    }
                    effects.add(effect);
                }
            }
            if(json.has("effects") && json.get("effects").isJsonArray()) {
                JsonArray array = json.getAsJsonArray("effects");
                for (JsonElement je : array) {
                    String idString = je.getAsString();
                    if (!idString.isEmpty()) {
                        Identifier id = Identifier.tryParse(idString);
                        StatusEffect effect = Registry.STATUS_EFFECT.get(id);
                        if (effect == null) {
                            throw new JsonSyntaxException("EffectImmunityPower could not find effect with id '" + id + "'.");
                        }
                        effects.add(effect);
                    }
                }
            }
            EffectImmunityPowerFactory factory = new EffectImmunityPowerFactory(effects);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

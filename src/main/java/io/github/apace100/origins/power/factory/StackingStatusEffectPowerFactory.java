package io.github.apace100.origins.power.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.StackingStatusEffectPower;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.LinkedList;
import java.util.List;

public class StackingStatusEffectPowerFactory extends PowerFactory<StackingStatusEffectPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "stacking_status_effect");

    private final int minStacks;
    private final int maxStacks;
    private final int durationPerStack;
    private final List<StatusEffectInstance> statusEffects;

    public StackingStatusEffectPowerFactory(int minStacks, int maxStacks, int durationPerStack, List<StatusEffectInstance> statusEffects) {
        this.minStacks = minStacks;
        this.maxStacks = maxStacks;
        this.durationPerStack = durationPerStack;
        this.statusEffects = statusEffects;
    }


    @Override
    public StackingStatusEffectPower apply(PowerType<StackingStatusEffectPower> powerType, PlayerEntity playerEntity) {
        StackingStatusEffectPower power = new StackingStatusEffectPower(powerType, playerEntity, minStacks, maxStacks, durationPerStack);
        for(StatusEffectInstance sei : statusEffects) {
            power.addEffect(sei);
        }
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<StackingStatusEffectPowerFactory> {

        @Override
        public void write(StackingStatusEffectPowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.minStacks);
            buf.writeInt(factory.maxStacks);
            buf.writeInt(factory.durationPerStack);
            buf.writeInt(factory.statusEffects.size());
            for(StatusEffectInstance sei : factory.statusEffects) {
                SerializationHelper.writeStatusEffect(sei, buf);
            }
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public StackingStatusEffectPowerFactory read(PacketByteBuf buf) {
            int minStacks = buf.readInt();
            int maxStacks = buf.readInt();
            int durationPerStack = buf.readInt();
            int effectCount = buf.readInt();
            List<StatusEffectInstance> effects = new LinkedList<>();
            for(int i = 0; i < effectCount; i++) {
                effects.add(SerializationHelper.readStatusEffect(buf));
            }
            StackingStatusEffectPowerFactory factory = new StackingStatusEffectPowerFactory(minStacks, maxStacks, durationPerStack, effects);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public StackingStatusEffectPowerFactory read(JsonObject json) {
            if(!json.has("min_stacks") || !json.get("min_stacks").isJsonPrimitive()) {
                throw new JsonSyntaxException("StackingStatusEffectPower json requires \"min_stacks\" integer.");
            }
            int minStacks = JsonHelper.getInt(json, "min_stacks");
            if(!json.has("max_stacks") || !json.get("max_stacks").isJsonPrimitive()) {
                throw new JsonSyntaxException("StackingStatusEffectPower json requires \"max_stacks\" integer.");
            }
            int maxStacks = JsonHelper.getInt(json, "max_stacks");
            int durationPerStack = JsonHelper.getInt(json, "duration_per_stack", 10);
            List<StatusEffectInstance> effects = new LinkedList<>();
            if(json.has("effect")) {
                if(json.get("effect").isJsonObject()) {
                    effects.add(SerializationHelper.readStatusEffect(json.getAsJsonObject("effect")));
                } else {
                    throw new JsonSyntaxException("StackingStatusEffectPower json requires \"effect\" to be an object.");
                }
            }
            if(json.has("effects")) {
                if(json.get("effects").isJsonArray()) {
                    JsonArray effectArray = json.getAsJsonArray("effects");
                    effectArray.forEach(je -> {
                        if(je.isJsonObject()) {
                            effects.add(SerializationHelper.readStatusEffect(je.getAsJsonObject()));
                        } else {
                            throw new JsonSyntaxException("StackingStatusEffectPower: \"effects\" array contained non-object entry.");
                        }
                    });
                } else {
                    throw new JsonSyntaxException("StackingStatusEffectPower json requires \"effects\" to be an array of objects.");
                }
            }
            StackingStatusEffectPowerFactory factory = new StackingStatusEffectPowerFactory(minStacks, maxStacks, durationPerStack, effects);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

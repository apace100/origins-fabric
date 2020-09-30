package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class StatusEffectCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "status_effect");

    private final StatusEffect effect;
    private final int minAmplifier;
    private final int maxAmplifier;
    private final int minDuration;
    private final int maxDuration;

    public StatusEffectCondition(Identifier statusEffectId, int minAmplifier, int maxAmplifier, int minDuration, int maxDuration) {
        this(Registry.STATUS_EFFECT.get(statusEffectId), minAmplifier, maxAmplifier, minDuration, maxDuration);
    }

    public StatusEffectCondition(StatusEffect effect, int minAmplifier, int maxAmplifier, int minDuration, int maxDuration) {
        this.effect = effect;
        this.minAmplifier = minAmplifier;
        this.maxAmplifier = maxAmplifier;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        if(this.effect == null) {
            return false;
        }
        if(playerEntity.hasStatusEffect(effect)) {
            StatusEffectInstance instance = playerEntity.getStatusEffect(effect);
            return instance.getDuration() <= maxDuration && instance.getDuration() >= minDuration
                && instance.getAmplifier() <= maxAmplifier && instance.getAmplifier() >= minAmplifier;
        }
        return false;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PlayerCondition.Serializer<StatusEffectCondition> {

        @Override
        public void write(StatusEffectCondition condition, PacketByteBuf buf) {
            buf.writeIdentifier(Registry.STATUS_EFFECT.getId(condition.effect));
            buf.writeInt(condition.minAmplifier);
            buf.writeInt(condition.maxAmplifier);
            buf.writeInt(condition.minDuration);
            buf.writeInt(condition.maxDuration);
        }

        @Override
        public StatusEffectCondition read(PacketByteBuf buf) {
            Identifier id = buf.readIdentifier();
            int minAmp = buf.readInt();
            int maxAmp = buf.readInt();
            int minDur = buf.readInt();
            int maxDur = buf.readInt();
            return new StatusEffectCondition(id, minAmp, maxAmp, minDur, maxDur);
        }

        @Override
        public StatusEffectCondition read(JsonObject json) {
            String effectId = JsonHelper.getString(json, "effect", "");
            if(effectId.isEmpty()) {
                throw new JsonSyntaxException("StatusEffectCondition json requires \"power\" identifier.");
            }
            int minAmp = JsonHelper.getInt(json, "min_amplifier", 0);
            int maxAmp = JsonHelper.getInt(json, "max_amplifier", Integer.MAX_VALUE);
            int minDur = JsonHelper.getInt(json, "min_duration", 0);
            int maxDur = JsonHelper.getInt(json, "max_duration", Integer.MAX_VALUE);
            return new StatusEffectCondition(Identifier.tryParse(effectId), minAmp, maxAmp, minDur, maxDur);
        }
    }
}

package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.FireProjectilePower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class FireProjectilePowerFactory extends PowerFactory<FireProjectilePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "fire_projectile");

    private final EntityType entityType;
    private final int cooldownDuration;
    private final int barIndex;
    private final int projectileCount;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;

    public FireProjectilePowerFactory(EntityType entityType, int cooldownDuration, int barIndex, int projectileCount, float speed, float divergence, SoundEvent soundEvent) {
        this.entityType = entityType;
        this.cooldownDuration = cooldownDuration;
        this.barIndex = barIndex;
        this.projectileCount = projectileCount;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
    }

    @Override
    public FireProjectilePower apply(PowerType<FireProjectilePower> powerType, PlayerEntity playerEntity) {
        FireProjectilePower power = new FireProjectilePower(powerType, playerEntity, cooldownDuration, barIndex, entityType, projectileCount, speed, divergence, soundEvent);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<FireProjectilePowerFactory> {

        @Override
        public void write(FireProjectilePowerFactory factory, PacketByteBuf buf) {
            buf.writeIdentifier(EntityType.getId(factory.entityType));
            buf.writeInt(factory.cooldownDuration);
            buf.writeInt(factory.barIndex);
            buf.writeInt(factory.projectileCount);
            buf.writeFloat(factory.speed);
            buf.writeFloat(factory.divergence);
            buf.writeBoolean(factory.soundEvent != null);
            if(factory.soundEvent != null) {
                buf.writeIdentifier(Registry.SOUND_EVENT.getId(factory.soundEvent));
            }
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public FireProjectilePowerFactory read(PacketByteBuf buf) {
            EntityType entityType = EntityType.get(buf.readIdentifier().toString()).get();
            int cooldownDuration = buf.readInt();
            int barIndex = buf.readInt();
            int projectileCount = buf.readInt();
            float speed = buf.readFloat();
            float divergence = buf.readFloat();
            SoundEvent soundEvent = null;
            if(buf.readBoolean()) {
                soundEvent = Registry.SOUND_EVENT.get(buf.readIdentifier());
            }
            FireProjectilePowerFactory factory = new FireProjectilePowerFactory(entityType, cooldownDuration, barIndex, projectileCount, speed, divergence, soundEvent);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public FireProjectilePowerFactory read(JsonObject json) {
            if(!json.has("entity_type")) {
                throw new JsonSyntaxException("FireProjectilePower json requires \"entity_type\" identifier.");
            }
            Optional<EntityType<?>> entityTypeOptional = EntityType.get(JsonHelper.getString(json, "entity_type"));
            if(!entityTypeOptional.isPresent()) {
                throw new JsonSyntaxException("FireProjectilePower json contained invalid \"entity_type\" identifier, was not registered.");
            }
            EntityType entityType = entityTypeOptional.get();
            int barIndex = JsonHelper.getInt(json, "bar_index", -1);
            int cooldownDuration = JsonHelper.getInt(json, "cooldown", 20);
            int projectileCount = JsonHelper.getInt(json, "count", 1);
            float speed = JsonHelper.getFloat(json, "speed", 1.5F);
            float divergence = JsonHelper.getFloat(json, "divergence", 1F);
            SoundEvent soundEvent = null;
            if(json.has("sound")) {
                Optional<SoundEvent> soundEventOptional = Registry.SOUND_EVENT.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "sound")));
                if(soundEventOptional.isPresent()) {
                    soundEvent = soundEventOptional.get();
                } else {
                    Origins.LOGGER.warn("FireProjectilePower json contained invalid \"sound\" identifier, was not registered.");
                }
            }
            FireProjectilePowerFactory factory = new FireProjectilePowerFactory(entityType, cooldownDuration, barIndex, projectileCount, speed, divergence, soundEvent);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ActiveCooldownPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class LaunchPowerFactory extends PowerFactory<ActiveCooldownPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "launch");

    private final int cooldownDuration;
    private final int barIndex;
    private final float speed;
    private final SoundEvent soundEvent;

    public LaunchPowerFactory(int cooldownDuration, int barIndex, float speed, SoundEvent soundEvent) {
        this.cooldownDuration = cooldownDuration;
        this.barIndex = barIndex;
        this.speed = speed;
        this.soundEvent = soundEvent;
    }

    @Override
    public ActiveCooldownPower apply(PowerType<ActiveCooldownPower> powerType, PlayerEntity playerEntity) {
        ActiveCooldownPower power = new ActiveCooldownPower(powerType, playerEntity, cooldownDuration, barIndex, p -> {
            if(!p.world.isClient) {
                p.addVelocity(0, speed, 0);
                p.velocityModified = true;
                if(soundEvent != null) {
                    p.world.playSound((PlayerEntity)null, p.getX(), p.getY(), p.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
                }
                for(int i = 0; i < 4; ++i) {
                    ((ServerWorld)p.world).spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getRandomBodyY(), p.getZ(), 8, p.getRandom().nextGaussian(), 0.0D, p.getRandom().nextGaussian(), 0.5);
                }
            }
        });
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<LaunchPowerFactory> {

        @Override
        public void write(LaunchPowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.cooldownDuration);
            buf.writeInt(factory.barIndex);
            buf.writeFloat(factory.speed);
            buf.writeBoolean(factory.soundEvent != null);
            if(factory.soundEvent != null) {
                buf.writeIdentifier(Registry.SOUND_EVENT.getId(factory.soundEvent));
            }
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public LaunchPowerFactory read(PacketByteBuf buf) {
            int cooldownDuration = buf.readInt();
            int barIndex = buf.readInt();
            float speed = buf.readFloat();
            SoundEvent soundEvent = null;
            if(buf.readBoolean()) {
                soundEvent = Registry.SOUND_EVENT.get(buf.readIdentifier());
            }
            LaunchPowerFactory factory = new LaunchPowerFactory(cooldownDuration, barIndex, speed, soundEvent);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public LaunchPowerFactory read(JsonObject json) {
            int barIndex = JsonHelper.getInt(json, "bar_index", -1);
            int cooldownDuration = JsonHelper.getInt(json, "cooldown", 20);
            float speed = JsonHelper.getFloat(json, "speed", 1.5F);
            SoundEvent soundEvent = null;
            if(json.has("sound")) {
                Optional<SoundEvent> soundEventOptional = Registry.SOUND_EVENT.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "sound")));
                if(soundEventOptional.isPresent()) {
                    soundEvent = soundEventOptional.get();
                } else {
                    Origins.LOGGER.warn("LaunchPower json contained invalid \"sound\" identifier, was not registered.");
                }
            }
            LaunchPowerFactory factory = new LaunchPowerFactory(cooldownDuration, barIndex, speed, soundEvent);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

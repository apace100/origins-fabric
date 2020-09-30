package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ParticlePower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class ParticlePowerFactory extends PowerFactory<ParticlePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "particle");

    private final ParticleEffect particle;
    private final int frequency;

    public ParticlePowerFactory(ParticleEffect particle, int frequency) {
        this.particle = particle;
        this.frequency = frequency;
    }

    @Override
    public ParticlePower apply(PowerType<ParticlePower> powerType, PlayerEntity playerEntity) {
        ParticlePower power = new ParticlePower(powerType, playerEntity, particle, frequency);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ParticlePowerFactory> {

        @Override
        public void write(ParticlePowerFactory factory, PacketByteBuf buf) {
            buf.writeIdentifier(Registry.PARTICLE_TYPE.getId(factory.particle.getType()));
            buf.writeInt(factory.frequency);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ParticlePowerFactory read(PacketByteBuf buf) {
            Identifier particleId = buf.readIdentifier();
            ParticleType type = Registry.PARTICLE_TYPE.get(particleId);
            int freq = buf.readInt();
            ParticlePowerFactory factory = new ParticlePowerFactory((ParticleEffect) type, freq);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ParticlePowerFactory read(JsonObject json) {
            if(!json.has("particle")) {
                throw new JsonSyntaxException("ParticlePower json requires \"particle\" identifier.");
            }
            Identifier particleId = Identifier.tryParse(JsonHelper.getString(json, "particle", "minecraft:entity_effect"));

            ParticleType type = Registry.PARTICLE_TYPE.get(particleId);
            if(!(type instanceof ParticleEffect)) {
                throw new JsonSyntaxException("ParticlePower json particle ID '" + particleId + "' is invalid: not an instance of ParticleEffect.");
            }
            if(!json.has("frequency")) {
                throw new JsonSyntaxException("ParticlePower json requires \"frequency\" integer.");
            }
            int freq = JsonHelper.getInt(json, "frequency");
            ParticlePowerFactory factory = new ParticlePowerFactory((ParticleEffect)type, freq);
            super.readConditions(factory, json);
            return factory;
        }
    }
}

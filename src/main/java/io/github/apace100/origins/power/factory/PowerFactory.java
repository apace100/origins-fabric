package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PowerFactory<P extends Power> {

    private final Identifier id;
    private boolean hasConditions = false;
    protected SerializableData data;
    protected Function<SerializableData.Instance, BiFunction<PowerType<P>, PlayerEntity, P>> factoryConstructor;

    public PowerFactory(Identifier id, SerializableData data, Function<SerializableData.Instance, BiFunction<PowerType<P>, PlayerEntity, P>> factoryConstructor) {
        this.id = id;
        this.data = data;
        this.factoryConstructor = factoryConstructor;
    }

    public PowerFactory<P> allowCondition() {
        if(!hasConditions) {
            hasConditions = true;
            data.add("condition", SerializableDataType.ENTITY_CONDITION, null);
        }
        return this;
    }

    public Identifier getSerializerId() {
        return id;
    }

    public class Instance implements BiFunction<PowerType<P>, PlayerEntity, P> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public void write(PacketByteBuf buf) {
            buf.writeIdentifier(id);
            data.write(buf, dataInstance);
        }

        @Override
        public P apply(PowerType<P> pPowerType, PlayerEntity playerEntity) {
            BiFunction<PowerType<P>, PlayerEntity, P> powerFactory = factoryConstructor.apply(dataInstance);
            P p = powerFactory.apply(pPowerType, playerEntity);
            if(hasConditions && dataInstance.isPresent("condition")) {
                p.addCondition((ConditionFactory<PlayerEntity>.Instance) dataInstance.get("condition"));
            }
            return p;
        }
    }

    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    public Instance read(PacketByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}

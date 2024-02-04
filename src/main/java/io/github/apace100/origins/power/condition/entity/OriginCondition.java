package io.github.apace100.origins.power.condition.entity;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class OriginCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        OriginComponent originComponent = ModComponents.ORIGIN.getNullable(entity);
        if (originComponent == null) {
            return false;
        }

        Identifier originId = data.get("origin");
        Identifier layerId = data.get("layer");

        if (layerId == null) {
            return originComponent.getOrigins().values()
                .stream()
                .map(Origin::getIdentifier)
                .anyMatch(originId::equals);
        }

        OriginLayer layer = OriginLayers.getNullableLayer(layerId);
        if (layer == null) {
            return false;
        }

        Origin origin = originComponent.getOrigin(layer);
        return origin != null
            && origin.getIdentifier().equals(originId);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Origins.identifier("origin"),
            new SerializableData()
                .add("origin", SerializableDataTypes.IDENTIFIER)
                .add("layer", SerializableDataTypes.IDENTIFIER, null),
            OriginCondition::condition
        );
    }

}

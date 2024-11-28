package io.github.apace100.origins.condition.entity.type;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.condition.factory.entity.OriginsEntityConditionTypes;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OriginEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<OriginEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("origin", SerializableDataTypes.IDENTIFIER)
            .add("layer", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty()),
        data -> new OriginEntityConditionType(
            data.get("origin"),
            data.get("layer")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("origin", conditionType.originId)
            .set("layer", conditionType.layerId)
    );

    private final Identifier originId;
    private final Optional<Identifier> layerId;

    public OriginEntityConditionType(Identifier originId, Optional<Identifier> layerId) {
        this.originId = originId;
        this.layerId = layerId;
    }

    @Override
    public boolean test(Entity entity) {

        OriginComponent originComponent = ModComponents.ORIGIN.getNullable(entity);
        if (originComponent == null) {
            return false;
        }

        return layerId
            .flatMap(OriginLayerManager::getOptional)
            .map(originComponent::getOrigin)
            .map(Origin::getId)
            .map(originId::equals)
            .orElseGet(() -> originComponent.getOrigins().values()
                .stream()
                .map(Origin::getId)
                .anyMatch(originId::equals));

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return OriginsEntityConditionTypes.ORIGIN;
    }

}

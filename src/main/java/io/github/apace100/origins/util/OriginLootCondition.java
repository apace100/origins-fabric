package io.github.apace100.origins.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OriginLootCondition implements LootCondition {

    public static final Codec<OriginLootCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("origin").forGetter(OriginLootCondition::getOrigin),
        Identifier.CODEC.optionalFieldOf("layer").forGetter(OriginLootCondition::getLayer)
    ).apply(instance, OriginLootCondition::new));
    public static final LootConditionType TYPE = new LootConditionType(CODEC);

    private final Identifier origin;
    private final Optional<Identifier> layer;

    private OriginLootCondition(Identifier origin, Optional<Identifier> layer) {
        this.origin = origin;
        this.layer = layer;
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {

        OriginComponent component = ModComponents.ORIGIN.maybeGet(lootContext.get(LootContextParameters.THIS_ENTITY))
            .orElse(null);
        if (component == null) {
            return false;
        }

        for (Map.Entry<OriginLayer, Origin> entry : component.getOrigins().entrySet()) {

            Identifier layerId = entry.getKey().getIdentifier();
            Identifier originId = entry.getValue().getIdentifier();

            if (layer.map(layerId::equals).orElse(true) && originId.equals(origin)) {
                return true;
            }

        }

        return false;

    }

    public Identifier getOrigin() {
        return origin;
    }

    public Optional<Identifier> getLayer() {
        return layer;
    }

}

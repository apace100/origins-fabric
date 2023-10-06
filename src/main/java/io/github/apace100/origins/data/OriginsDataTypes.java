package io.github.apace100.origins.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginUpgrade;
import net.minecraft.util.Identifier;

import java.util.List;

public final class OriginsDataTypes {

    public static final SerializableDataType<Impact> IMPACT = SerializableDataType.enumValue(Impact.class);

    public static final SerializableDataType<OriginUpgrade> UPGRADE = SerializableDataType.compound(
        OriginUpgrade.class,
        OriginUpgrade.DATA,
        OriginUpgrade::fromData,
        (serializableData, originUpgrade) -> originUpgrade.toData()
    );

    public static final SerializableDataType<List<OriginUpgrade>> UPGRADES = SerializableDataType.list(UPGRADE);

    public static final SerializableDataType<OriginLayer.ConditionedOrigin> CONDITIONED_ORIGIN = SerializableDataType.compound(
        OriginLayer.ConditionedOrigin.class,
        OriginLayer.ConditionedOrigin.DATA,
        OriginLayer.ConditionedOrigin::fromData,
        (serializableData, conditionedOrigin) -> conditionedOrigin.toData()
    );

    public static final SerializableDataType<List<OriginLayer.ConditionedOrigin>> CONDITIONED_ORIGINS = SerializableDataType.list(CONDITIONED_ORIGIN);

    public static final SerializableDataType<OriginLayer.ConditionedOrigin> ORIGIN_OR_CONDITIONED_ORIGIN = new SerializableDataType<>(
        OriginLayer.ConditionedOrigin.class,
        CONDITIONED_ORIGIN::send,
        CONDITIONED_ORIGIN::receive,
        jsonElement -> {

            if (jsonElement instanceof JsonObject jsonObject) {
                return CONDITIONED_ORIGIN.read(jsonObject);
            }

            if (!(jsonElement instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                throw new JsonSyntaxException("Expected a JSON object or string.");
            }

            Identifier originId = SerializableDataTypes.IDENTIFIER.read(jsonPrimitive);
            return new OriginLayer.ConditionedOrigin(null, Lists.newArrayList(originId));

        },
        CONDITIONED_ORIGIN::write
    );

    public static final SerializableDataType<List<OriginLayer.ConditionedOrigin>> ORIGINS_OR_CONDITIONED_ORIGINS = SerializableDataType.list(ORIGIN_OR_CONDITIONED_ORIGIN);

    public static final SerializableDataType<OriginLayer.GuiTitle> GUI_TITLE = SerializableDataType.compound(
        OriginLayer.GuiTitle.class,
        OriginLayer.GuiTitle.DATA,
        OriginLayer.GuiTitle::fromData,
        (serializableData, guiTitle) -> guiTitle.toData()
    );

}

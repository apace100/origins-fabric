package io.github.apace100.origins.data;

import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.OriginUpgrade;

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

}

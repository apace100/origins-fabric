package io.github.apace100.origins.util;

import net.minecraft.screen.ScreenHandlerType;

public enum InventoryType {

    THREE_BY_THREE(ScreenHandlerType.GENERIC_3X3),
    NINE_BY_ONE(ScreenHandlerType.GENERIC_9X1),
    NINE_BY_TWO(ScreenHandlerType.GENERIC_9X2),
    NINE_BY_THREE(ScreenHandlerType.GENERIC_9X3),
    NINE_BY_FOUR(ScreenHandlerType.GENERIC_9X4),
    NINE_BY_FIVE(ScreenHandlerType.GENERIC_9X5),
    NINE_BY_SIX(ScreenHandlerType.GENERIC_9X6);

    private final ScreenHandlerType type;

    InventoryType(ScreenHandlerType type) {
        this.type = type;
    }

    public ScreenHandlerType getType() {
        return type;
    }
}

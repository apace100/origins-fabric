package io.github.apace100.origins.mixin;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingInventory.class)
public interface CraftingInventoryAccessor {

    @Accessor
    ScreenHandler getHandler();
}

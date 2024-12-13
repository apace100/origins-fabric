package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.origins.component.item.ItemOriginsComponent;
import io.github.apace100.origins.registry.ModDataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private TypedActionResult<ItemStack> origins$selectItemOrigins(TypedActionResult<ItemStack> original, World world, PlayerEntity user, Hand hand) {

        if (original.getResult() != ActionResult.PASS) {
            return original;
        }

        ItemStack stack = original.getValue();
        ItemOriginsComponent itemOrigins = stack.get(ModDataComponentTypes.ORIGIN);

        if (itemOrigins == null) {
            return original;
        }

        itemOrigins.setOrigin(user);
        user.incrementStat(Stats.USED.getOrCreateStat((Item) (Object) this));

        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.consume(stack);

    }

}

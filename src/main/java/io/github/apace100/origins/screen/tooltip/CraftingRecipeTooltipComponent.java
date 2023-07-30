package io.github.apace100.origins.screen.tooltip;

import io.github.apace100.origins.Origins;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**A {@link TooltipComponent} used for {@link io.github.apace100.origins.badge.CraftingRecipeBadge}
 * Draws a snapshot of a 3x3 crafting recipe in the tooltip*/
public class CraftingRecipeTooltipComponent implements TooltipComponent {
    private final int recipeWidth;
    private final DefaultedList<ItemStack> inputs;
    private final ItemStack output;
    private static final Identifier TEXTURE = Origins.identifier("textures/gui/tooltip/recipe_tooltip.png");

    public CraftingRecipeTooltipComponent(int recipeWidth, DefaultedList<ItemStack> inputs, ItemStack output) {
        this.recipeWidth = recipeWidth;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public int getHeight() {
        return 68;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 130;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        this.drawBackground(context, x, y);
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                int index = column + row * recipeWidth;
                int slotX = x + 8 + column * 18;
                int slotY = y + 8 + row * 18;
                ItemStack stack = column >= recipeWidth ? ItemStack.EMPTY : inputs.get(index);
                context.drawItem(stack, slotX, slotY);
                context.drawItemInSlot(textRenderer, stack, slotX, slotY);
            }
        }
        context.drawItem(output, x + 101, y + 25);
        context.drawItemInSlot(textRenderer, output, x + 101, y + 25);
    }

    public void drawBackground(DrawContext context, int x, int y) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.drawTexture(TEXTURE, x, y, 0, 0, 130, 86, 256, 256);
    }

}

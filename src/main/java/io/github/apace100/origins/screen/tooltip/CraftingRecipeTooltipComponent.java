package io.github.apace100.origins.screen.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**A {@link TooltipComponent} used for {@link io.github.apace100.origins.screen.badge.CraftingRecipeBadge}
 * Draws a snapshot of a 3x3 crafting recipe in the tooltip*/
public class CraftingRecipeTooltipComponent implements TooltipComponent {
    private final DefaultedList<ItemStack> inputs;
    private final ItemStack output;
    private static final Identifier TEXTURE = Origins.identifier("textures/gui/tooltip/recipe_tooltip.png");

    public CraftingRecipeTooltipComponent(DefaultedList<ItemStack> inputs, ItemStack output) {
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
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        this.drawBackGround(matrices, x, y, z);
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                int index = column + row * 3;
                int slotX = x + 8 + column * 18;
                int slotY = y + 8 + row * 18;
                ItemStack stack = inputs.get(index);
                itemRenderer.renderInGuiWithOverrides(stack, slotX, slotY, index);
                itemRenderer.renderGuiItemOverlay(textRenderer, stack, slotX, slotY);
            }
        }
        itemRenderer.renderInGuiWithOverrides(output, x + 101, y + 25, 10);
    }

    public void drawBackGround(MatrixStack matrices, int x, int y, int z) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, z, 0, 0, 130, 86, 256, 256);
    }

}

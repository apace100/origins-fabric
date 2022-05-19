package io.github.apace100.origins.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Invoker("renderTooltipFromComponents")
    void invokeRenderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y);

}

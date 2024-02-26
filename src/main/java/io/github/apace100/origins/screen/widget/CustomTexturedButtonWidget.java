package io.github.apace100.origins.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class CustomTexturedButtonWidget extends ButtonWidget {

    protected ButtonTextures textures;

    public static CustomTexturedButtonWidget.Builder builder(Text message, ButtonTextures buttonTextures, PressAction onPress) {
        return new CustomTexturedButtonWidget.Builder(message, buttonTextures, onPress);
    }

    protected CustomTexturedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, ButtonTextures buttonTextures, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
        this.textures = buttonTextures;
    }

    public void setTextures(ButtonTextures textures) {
        this.textures = textures;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        Identifier identifier = this.textures.get(this.isNarratable(), this.isSelected());
        context.drawGuiTexture(identifier, this.getX(), this.getY(), this.width, this.height);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }

    @Environment(value= EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        private final ButtonTextures buttonTextures;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message, ButtonTextures buttonTextures, PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
            this.buttonTextures = buttonTextures;
        }

        public CustomTexturedButtonWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public CustomTexturedButtonWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        public CustomTexturedButtonWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public CustomTexturedButtonWidget.Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public CustomTexturedButtonWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CustomTexturedButtonWidget.Builder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public CustomTexturedButtonWidget build() {
            CustomTexturedButtonWidget buttonWidget = new CustomTexturedButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.buttonTextures, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }
}

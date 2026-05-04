package io.github.apace100.origins.util;

import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public class MiscUtilClient {

	public static void drawScrollingText(DrawContext context, TextRenderer renderer, Text text, TextAlignment alignment, int startX, int startY, int endX, int endY, int color, boolean shadow) {

		int textWidth = renderer.getWidth(text);

		int height = (startY + endY - 9) / 2 + 1;
		int width = endX - startX;

		Optional<Integer> horizontalAlignment = alignment.horizontal(startX, endX, textWidth);

		if (textWidth <= width && horizontalAlignment.isPresent()) {
			context.drawText(renderer, text, horizontalAlignment.get(), height, color, shadow);
		}

		else {

			int horizontalDiff = textWidth - width;

			double d = (double) Util.getMeasuringTimeMs() / 1000.0;
			double e = Math.max((double) horizontalDiff * 0.5, 3.0);
			double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
			double g = MathHelper.lerp(f, 0.0, horizontalDiff);

			context.enableScissor(startX, startY, endX, endY);
			context.drawText(renderer, text, startX - (int) g, height, color, shadow);
			context.disableScissor();

		}

	}

}

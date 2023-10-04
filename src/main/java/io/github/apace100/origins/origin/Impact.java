package io.github.apace100.origins.origin;

import io.github.apace100.origins.Origins;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public enum Impact {
	
	NONE(0, "none", Formatting.GRAY, Origins.identifier("choose_origin/impact/none")),
	LOW(1, "low", Formatting.GREEN, Origins.identifier("choose_origin/impact/low")),
	MEDIUM(2, "medium", Formatting.YELLOW, Origins.identifier("choose_origin/impact/medium")),
	HIGH(3, "high", Formatting.RED, Origins.identifier("choose_origin/impact/high"));
	
	private final int impactValue;
	private final String translationKey;
	private final Formatting textStyle;
	private final Identifier spriteId;

	private Impact(int impactValue, String translationKey, Formatting textStyle, Identifier spriteId) {
		this.translationKey = "origins.gui.impact." + translationKey;
		this.impactValue = impactValue;
		this.textStyle = textStyle;
		this.spriteId = spriteId;
	}

	public Identifier getSpriteId() {
		return spriteId;
	}

	public int getImpactValue() {
		return impactValue;
	}
	
	public String getTranslationKey() {
		return translationKey;
	}
	
	public Formatting getTextStyle() {
		return textStyle;
	}
	
	public MutableText getTextComponent() {
		return Text.translatable(getTranslationKey()).formatted(getTextStyle());
	}
	
	public static Impact getByValue(int impactValue) {
		return Impact.values()[impactValue];
	}
}

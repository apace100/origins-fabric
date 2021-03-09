package io.github.apace100.origins.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ViewOriginScreen extends Screen {

	private static final Identifier WINDOW = new Identifier(Origins.MODID, "textures/gui/choose_origin.png");

	private ArrayList<Pair<OriginLayer, Origin>> originLayers;
	private int currentLayer = 0;
	private static final int windowWidth = 176;
	private static final int windowHeight = 182;
	private int scrollPos = 0;
	private int currentMaxScroll = 0;
	private int border = 13;

	private ButtonWidget chooseOriginButton;

	private int guiTop, guiLeft;

	public ViewOriginScreen() {
		super(new TranslatableText(Origins.MODID + ".screen.view_origin"));
		HashMap<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(MinecraftClient.getInstance().player).getOrigins();
		originLayers = new ArrayList<>(origins.size());
		PlayerEntity player = MinecraftClient.getInstance().player;
		origins.forEach((layer, origin) -> {
			if(origin.getDisplayItem().getItem() == Items.PLAYER_HEAD) {
				origin.getDisplayItem().getOrCreateTag().putString("SkullOwner", player.getDisplayName().getString());
			}
			if(origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0) {
				originLayers.add(new Pair<>(layer, origin));
			}
		});
		originLayers.sort(Comparator.comparing(Pair::getLeft));
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft = (this.width - windowWidth) / 2;
        guiTop = (this.height - windowHeight) / 2;
        if(originLayers.size() > 0) {
			addButton(chooseOriginButton = new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight - 40, 100, 20, new TranslatableText(Origins.MODID + ".gui.choose"), b -> {
				MinecraftClient.getInstance().openScreen(new ChooseOriginScreen(Lists.newArrayList(originLayers.get(currentLayer).getLeft()), 0, false));
			}));
			PlayerEntity player = MinecraftClient.getInstance().player;
			chooseOriginButton.active = chooseOriginButton.visible = originLayers.get(currentLayer).getRight() == Origin.EMPTY && originLayers.get(currentLayer).getLeft().getOriginOptionCount(player) > 0;
			addButton(new ButtonWidget(guiLeft - 40,this.height / 2 - 10, 20, 20, new LiteralText("<"), b -> {
				currentLayer = (currentLayer - 1 + originLayers.size()) % originLayers.size();
				chooseOriginButton.active = chooseOriginButton.visible = originLayers.get(currentLayer).getRight() == Origin.EMPTY && originLayers.get(currentLayer).getLeft().getOriginOptionCount(player) > 0;
				scrollPos = 0;
			}));
			addButton(new ButtonWidget(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20, new LiteralText(">"), b -> {
				currentLayer = (currentLayer + 1) % originLayers.size();
				chooseOriginButton.active = chooseOriginButton.visible = originLayers.get(currentLayer).getRight() == Origin.EMPTY && originLayers.get(currentLayer).getLeft().getOriginOptionCount(player) > 0;
				scrollPos = 0;
			}));
		}
        addButton(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, new TranslatableText(Origins.MODID + ".gui.close"), b -> {
			MinecraftClient.getInstance().openScreen(null);
        }));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.renderOriginWindow(matrices, mouseX, mouseY);
		super.render(matrices, mouseX, mouseY, delta);
	}

	private void renderOriginWindow(MatrixStack matrices, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		boolean hasLayer = originLayers.size() > 0;
		if(hasLayer && OriginsClient.isServerRunningOrigins) {
			renderWindowBackground(matrices, 16, 0);
			this.renderOriginContent(matrices, mouseX, mouseY);
			this.client.getTextureManager().bindTexture(WINDOW);
			this.drawTexture(matrices, guiLeft, guiTop, 0, 0, windowWidth, windowHeight);
			renderOriginName(matrices);
			this.client.getTextureManager().bindTexture(WINDOW);
			this.renderOriginImpact(matrices, mouseX, mouseY);
			Text title = new TranslatableText(Origins.MODID + ".gui.view_origin.title", new TranslatableText(originLayers.get(currentLayer).getLeft().getTranslationKey()));
			drawCenteredString(matrices, this.textRenderer, title.getString(), width / 2, guiTop - 15, 0xFFFFFF);
		} else {
			if(OriginsClient.isServerRunningOrigins) {
				drawCenteredString(matrices, this.textRenderer, new TranslatableText(Origins.MODID + ".gui.view_origin.empty").getString(), width / 2, guiTop + 15, 0xFFFFFF);
			} else {
				drawCenteredString(matrices, this.textRenderer, new TranslatableText(Origins.MODID + ".gui.view_origin.not_installed").getString(), width / 2, guiTop + 15, 0xFFFFFF);
			}
		}
		RenderSystem.disableBlend();
	}

	private Origin getCurrentOrigin() {
		return originLayers.get(currentLayer).getRight();
	}
	
	private void renderOriginImpact(MatrixStack matrices, int mouseX, int mouseY) {
		Impact impact = getCurrentOrigin().getImpact();
		int impactValue = impact.getImpactValue();
		int wOffset = impactValue * 8;
		for(int i = 0; i < 3; i++) {
			if(i < impactValue) {
				this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth + wOffset, 16, 8, 8);
			} else {
				this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth, 16, 8, 8);
			}
		}
		if(mouseX >= guiLeft + 128 && mouseX <= guiLeft + 158
		&& mouseY >= guiTop + 19 && mouseY <= guiTop + 27) {
			TranslatableText ttc = (TranslatableText) new TranslatableText(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
			this.renderTooltip(matrices, ttc, mouseX, mouseY);
		}
	}
	
	private void renderOriginName(MatrixStack matrices) {
		Origin origin = getCurrentOrigin();
		StringVisitable originName = textRenderer.trimToWidth(origin == Origin.EMPTY ? new TranslatableText(originLayers.get(currentLayer).getLeft().getMissingOriginNameTranslationKey()) : origin.getName(), windowWidth - 36);
		this.drawStringWithShadow(matrices, textRenderer, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFF);
		ItemStack is = origin.getDisplayItem();
		this.itemRenderer.renderInGui(is, guiLeft + 15, guiTop + 15);
	}
	
	private void renderWindowBackground(MatrixStack matrices, int offsetYStart, int offsetYEnd) {
		int endX = guiLeft + windowWidth - border;
		int endY = guiTop + windowHeight - border;
		this.client.getTextureManager().bindTexture(WINDOW);
		for(int x = guiLeft; x < endX; x += 16) {
			for(int y = guiTop + offsetYStart; y < endY + offsetYEnd; y += 16) {
				this.drawTexture(matrices, x, y, windowWidth, 0, Math.max(16, endX - x), Math.max(16, endY + offsetYEnd - y));
			}
		}
	}
	
	@Override
	public boolean mouseScrolled(double x, double y, double z) {
		boolean retValue = super.mouseScrolled(x, y, z);
		int np = this.scrollPos - (int)z * 4;
		this.scrollPos = np < 0 ? 0 : Math.min(np, this.currentMaxScroll);
		return retValue;
	}

	private void renderOriginContent(MatrixStack matrices, int mouseX, int mouseY) {
		int x = guiLeft + 18;
		int y = guiTop + 50;
		int startY = y;
		int endY = y - 72 + windowHeight;
		y -= scrollPos;

		Origin origin = getCurrentOrigin();


		Text orgDesc = origin.getDescription();
		if(origin == Origin.EMPTY) {
			orgDesc = new TranslatableText(originLayers.get(currentLayer).getLeft().getMissingOriginDescriptionTranslationKey());
		}
		List<OrderedText> descLines = textRenderer.wrapLines(orgDesc, windowWidth - 36);
		for(OrderedText line : descLines) {
			if(y >= startY - 18 && y <= endY + 12) {
				textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);
			}
			y += 12;
		}
		if(origin == Origin.EMPTY) {
			return;
		}
		for(PowerType<?> p : origin.getPowerTypes()) {
			if(p.isHidden()) {
				continue;
			}
			OrderedText name = Language.getInstance().reorder(textRenderer.trimToWidth(p.getName().formatted(Formatting.UNDERLINE), windowWidth - 36));
			Text desc = p.getDescription();
			List<OrderedText> drawLines = textRenderer.wrapLines(desc, windowWidth - 36);
			if(y >= startY - 24 && y <= endY + 12) {
				textRenderer.draw(matrices, name, x, y, 0xFFFFFF);
			}
			for(OrderedText line : drawLines) {
				y += 12;
				if(y >= startY - 24 && y <= endY + 12) {
					textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
				}
			}

			y += 14;
			
		}
		y += scrollPos;
		currentMaxScroll = y - windowHeight - 15;
		if(currentMaxScroll < 0) {
			currentMaxScroll = 0;
		}
	}
}

package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.PowerType;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class ChooseOriginScreen extends Screen {

	private static final Identifier WINDOW = new Identifier(Origins.MODID, "textures/gui/choose_origin.png");

	private ArrayList<OriginLayer> layerList;
	private int currentLayerIndex = 0;
	private int currentOrigin = 0;
	private List<Origin> originSelection;
	private static final int windowWidth = 176;
	private static final int windowHeight = 182;
	private int scrollPos = 0;
	private int currentMaxScroll = 0;
	private int border = 13;
	
	private int guiTop, guiLeft;

	private boolean showDirtBackground;
	
	public ChooseOriginScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
		super(new TranslatableText(Origins.MODID + ".screen.choose_origin"));
		this.layerList = layerList;
		this.currentLayerIndex = currentLayerIndex;
		this.originSelection = new ArrayList<>(10);
		layerList.get(currentLayerIndex).getOrigins().forEach(originId -> {
			Origin origin = OriginRegistry.get(originId);
			if(origin.isChoosable()) {
				if(origin.getDisplayItem().getItem() == Items.PLAYER_HEAD) {
					origin.getDisplayItem().getOrCreateTag().putString("SkullOwner", MinecraftClient.getInstance().player.getDisplayName().getString());
				}
				this.originSelection.add(origin);
			}
		});
		originSelection.sort((a, b) -> {
			int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
			return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
		});
		this.showDirtBackground = showDirtBackground;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft = (this.width - windowWidth) / 2;
        guiTop = (this.height - windowHeight) / 2;
        addButton(new ButtonWidget(guiLeft - 40,this.height / 2 - 10, 20, 20, new LiteralText("<"), b -> {
        	currentOrigin = (currentOrigin - 1 + originSelection.size()) % originSelection.size();
        	scrollPos = 0;
        }));
        addButton(new ButtonWidget(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20, new LiteralText(">"), b -> {
        	currentOrigin = (currentOrigin + 1) % originSelection.size();
        	scrollPos = 0;
        }));
        addButton(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, new TranslatableText(Origins.MODID + ".gui.select"), b -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeString(originSelection.get(currentOrigin).getIdentifier().toString());
			buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
			ClientSidePacketRegistry.INSTANCE.sendToServer(ModPackets.CHOOSE_ORIGIN, buf);
			if(currentLayerIndex + 1 >= layerList.size()) {
				MinecraftClient.getInstance().openScreen(null);
			} else {
				MinecraftClient.getInstance().openScreen(new ChooseOriginScreen(layerList, currentLayerIndex + 1, showDirtBackground));
			}
        }));
	}

	@Override
	public void renderBackground(MatrixStack matrices, int vOffset) {
		if(showDirtBackground) {
			super.renderBackgroundTexture(vOffset);
		} else {
			super.renderBackground(matrices, vOffset);
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.renderOriginWindow(matrices, mouseX, mouseY);
		super.render(matrices, mouseX, mouseY, delta);
	}

	private void renderOriginWindow(MatrixStack matrices, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		renderWindowBackground(matrices, 16, 0);
		this.renderOriginContent(matrices, mouseX, mouseY);
		this.client.getTextureManager().bindTexture(WINDOW);
		this.drawTexture(matrices, guiLeft, guiTop, 0, 0, windowWidth, windowHeight);
		renderOriginName(matrices);
		this.client.getTextureManager().bindTexture(WINDOW);
		this.renderOriginImpact(matrices, mouseX, mouseY);
		Text title = new TranslatableText(Origins.MODID + ".gui.choose_origin.title", new TranslatableText(layerList.get(currentLayerIndex).getTranslationKey()));
		this.drawCenteredString(matrices, this.textRenderer, title.getString(), width / 2, guiTop - 15, 0xFFFFFF);
		RenderSystem.disableBlend();
	}
	
	private void renderOriginImpact(MatrixStack matrices, int mouseX, int mouseY) {
		Impact impact = originSelection.get(currentOrigin).getImpact();
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
		StringVisitable originName = textRenderer.trimToWidth(originSelection.get(currentOrigin).getName(), windowWidth - 36);
		this.drawStringWithShadow(matrices, textRenderer, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFF);
		ItemStack is = originSelection.get(currentOrigin).getDisplayItem();
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
		Origin origin = originSelection.get(currentOrigin);
		int x = guiLeft + 18;
		int y = guiTop + 50;
		int startY = y;
		int endY = y - 72 + windowHeight;
		y -= scrollPos;
		
		Text orgDesc = origin.getDescription();
		List<OrderedText> descLines = textRenderer.wrapLines(orgDesc, windowWidth - 36);
		for(OrderedText line : descLines) {
			if(y >= startY - 18 && y <= endY + 12) {
				textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);
			}
			y += 12;
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

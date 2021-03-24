package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
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
	private int maxSelection = 0;
	private static final int windowWidth = 176;
	private static final int windowHeight = 182;
	private int scrollPos = 0;
	private int currentMaxScroll = 0;
	private int border = 13;
	
	private int guiTop, guiLeft;

	private boolean showDirtBackground;

	private Origin randomOrigin;
	private MutableText randomOriginText;
	
	public ChooseOriginScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
		super(new TranslatableText(Origins.MODID + ".screen.choose_origin"));
		this.layerList = layerList;
		this.currentLayerIndex = currentLayerIndex;
		this.originSelection = new ArrayList<>(10);
		PlayerEntity player = MinecraftClient.getInstance().player;
		OriginLayer currentLayer = layerList.get(currentLayerIndex);
		List<Identifier> originIdentifiers = currentLayer.getOrigins(player);
		originIdentifiers.forEach(originId -> {
			Origin origin = OriginRegistry.get(originId);
			if(origin.isChoosable()) {
				ItemStack displayItem = origin.getDisplayItem();
				if(displayItem.getItem() == Items.PLAYER_HEAD) {
					if(!displayItem.hasTag() || !displayItem.getTag().contains("SkullOwner")) {
						displayItem.getOrCreateTag().putString("SkullOwner", player.getDisplayName().getString());
					}
				}
				this.originSelection.add(origin);
			}
		});
		originSelection.sort((a, b) -> {
			int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
			return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
		});
		maxSelection = originSelection.size();
		if(currentLayer.isRandomAllowed() && currentLayer.getRandomOrigins(player).size() > 0) {
			maxSelection += 1;
		}
		if(maxSelection == 0) {
			openNextLayerScreen();
			return;
		}
		this.showDirtBackground = showDirtBackground;
	}

	private void openNextLayerScreen() {
		MinecraftClient.getInstance().openScreen(new WaitForNextLayerScreen(layerList, currentLayerIndex, showDirtBackground));
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
        	currentOrigin = (currentOrigin - 1 + maxSelection) % maxSelection;
        	scrollPos = 0;
        }));
        addButton(new ButtonWidget(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20, new LiteralText(">"), b -> {
        	currentOrigin = (currentOrigin + 1) % maxSelection;
        	scrollPos = 0;
        }));
        addButton(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, new TranslatableText(Origins.MODID + ".gui.select"), b -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			if(currentOrigin == originSelection.size()) {
				buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
				ClientPlayNetworking.send(ModPackets.CHOOSE_RANDOM_ORIGIN, buf);
			} else {
				buf.writeString(getCurrentOrigin().getIdentifier().toString());
				buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
				ClientPlayNetworking.send(ModPackets.CHOOSE_ORIGIN, buf);
			}
			openNextLayerScreen();
        }));
	}

	private Origin getCurrentOrigin() {
		if(currentOrigin == originSelection.size()) {
			if(randomOrigin == null) {
				initRandomOrigin();
			}
			return randomOrigin;
		}
		return originSelection.get(currentOrigin);
	}

	private void initRandomOrigin() {
		this.randomOrigin = new Origin(Origins.identifier("random"), new ItemStack(ModItems.ORB_OF_ORIGIN), Impact.NONE, -1, Integer.MAX_VALUE);
		this.randomOriginText = new LiteralText("");
		List<Identifier> randoms = layerList.get(currentLayerIndex).getRandomOrigins(MinecraftClient.getInstance().player);
		randoms.sort((ia, ib) -> {
			Origin a = OriginRegistry.get(ia);
			Origin b = OriginRegistry.get(ib);
			int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
			return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
		});
		for(Identifier id : randoms) {
			this.randomOriginText.append(OriginRegistry.get(id).getName());
			this.randomOriginText.append(new LiteralText("\n"));
		}
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
		if(maxSelection == 0) {
			openNextLayerScreen();
			return;
		}
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
		StringVisitable originName = textRenderer.trimToWidth(getCurrentOrigin().getName(), windowWidth - 36);
		this.drawStringWithShadow(matrices, textRenderer, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFF);
		ItemStack is = getCurrentOrigin().getDisplayItem();
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
		Origin origin = getCurrentOrigin();
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

		if(origin == randomOrigin) {
			List<OrderedText> drawLines = textRenderer.wrapLines(randomOriginText, windowWidth - 36);
			for(OrderedText line : drawLines) {
				y += 12;
				if(y >= startY - 24 && y <= endY + 12) {
					textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
				}
			}
			y += 14;
		} else {
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
		}
		y += scrollPos;
		currentMaxScroll = y - windowHeight - 15;
		if(currentMaxScroll < 0) {
			currentMaxScroll = 0;
		}
	}
}

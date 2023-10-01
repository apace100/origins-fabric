package io.github.apace100.origins.screen;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ChooseOriginScreen extends OriginDisplayScreen {

	private final ArrayList<OriginLayer> layerList;
	private int currentLayerIndex = 0;
	private int currentOriginIndex = 0;
	private final List<Origin> originSelection;
	private int maxSelection = 0;

	private Origin randomOrigin;
	
	public ChooseOriginScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
		super(Text.translatable(Origins.MODID + ".screen.choose_origin"), showDirtBackground);
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
					if(!displayItem.hasNbt() || !displayItem.getNbt().contains("SkullOwner")) {
						displayItem.getOrCreateNbt().putString("SkullOwner", player.getDisplayName().getString());
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
		}
		Origin newOrigin = getCurrentOriginInternal();
		showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
	}

	private void openNextLayerScreen() {
		MinecraftClient.getInstance().setScreen(new WaitForNextLayerScreen(layerList, currentLayerIndex, this.showDirtBackground));
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {
		super.init();
		if(maxSelection > 1) {
			addDrawableChild(ButtonWidget.builder(Text.of("<"), b -> {
				currentOriginIndex = (currentOriginIndex - 1 + maxSelection) % maxSelection;
				Origin newOrigin = getCurrentOriginInternal();
				showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
			}).dimensions(guiLeft - 40, this.height / 2 - 10, 20, 20).build());
			addDrawableChild(ButtonWidget.builder(Text.of(">"), b -> {
				currentOriginIndex = (currentOriginIndex + 1) % maxSelection;
				Origin newOrigin = getCurrentOriginInternal();
				showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
			}).dimensions(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20).build());
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.select"), b -> {

			Identifier originId = getCurrentOrigin().getIdentifier();
			Identifier layerId = layerList.get(currentLayerIndex).getIdentifier();

			if (currentOriginIndex == originSelection.size()) {
				ClientPlayNetworking.send(new ChooseRandomOriginC2SPacket(layerId));
			} else {
				ClientPlayNetworking.send(new ChooseOriginC2SPacket(layerId, originId));
			}

			openNextLayerScreen();

		}).dimensions(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20).build());
	}

	@Override
	protected Text getTitleText() {
		if (getCurrentLayer().shouldOverrideChooseOriginTitle()) {
			return Text.translatable(getCurrentLayer().getTitleChooseOriginTranslationKey());
		}
		return Text.translatable(Origins.MODID + ".gui.choose_origin.title", Text.translatable(getCurrentLayer().getTranslationKey()));
	}

	private Origin getCurrentOriginInternal() {
		if(currentOriginIndex == originSelection.size()) {
			if(randomOrigin == null) {
				initRandomOrigin();
			}
			return randomOrigin;
		}
		return originSelection.get(currentOriginIndex);
	}

	private void initRandomOrigin() {
		this.randomOrigin = new Origin(Origins.identifier("random"), new ItemStack(ModItems.ORB_OF_ORIGIN), Impact.NONE, -1, Integer.MAX_VALUE);
		MutableText randomOriginText = (MutableText)Text.of("");
		List<Identifier> randoms = layerList.get(currentLayerIndex).getRandomOrigins(MinecraftClient.getInstance().player);
		randoms.sort((ia, ib) -> {
			Origin a = OriginRegistry.get(ia);
			Origin b = OriginRegistry.get(ib);
			int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
			return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
		});
		for(Identifier id : randoms) {
			randomOriginText.append(OriginRegistry.get(id).getName());
			randomOriginText.append(Text.of("\n"));
		}
		setRandomOriginText(randomOriginText);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if(maxSelection == 0) {
			openNextLayerScreen();
			return;
		}
		super.render(context, mouseX, mouseY, delta);
	}
}

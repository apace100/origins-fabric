package io.github.apace100.origins.screen;

import com.google.common.collect.Lists;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ViewOriginScreen extends OriginDisplayScreen {

	private final ArrayList<Pair<OriginLayer, Origin>> originLayers;
	private int currentLayer = 0;
	private ButtonWidget chooseOriginButton;

	public ViewOriginScreen() {
		super(new TranslatableText(Origins.MODID + ".screen.view_origin"), false);
		PlayerEntity player = MinecraftClient.getInstance().player;
		HashMap<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(player).getOrigins();
		originLayers = new ArrayList<>(origins.size());

		origins.forEach((layer, origin) -> {
			ItemStack displayItem = origin.getDisplayItem();
			if(displayItem.getItem() == Items.PLAYER_HEAD) {
				if(!displayItem.hasNbt() || !displayItem.getNbt().contains("SkullOwner")) {
					displayItem.getOrCreateNbt().putString("SkullOwner", player.getDisplayName().getString());
				}
			}
			if((origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0) && !layer.isHidden()) {
				originLayers.add(new Pair<>(layer, origin));
			}
		});
		originLayers.sort(Comparator.comparing(Pair::getLeft));
		if(this.originLayers.size() > 0) {
			Pair<OriginLayer, Origin> current = originLayers.get(currentLayer);
			showOrigin(current.getRight(), current.getLeft(), false);
		} else {
			showOrigin(null, null, false);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
        if(originLayers.size() > 0 && OriginsClient.isServerRunningOrigins) {
			addDrawableChild(chooseOriginButton = new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight - 40, 100, 20, new TranslatableText(Origins.MODID + ".gui.choose"), b -> {
				MinecraftClient.getInstance().setScreen(new ChooseOriginScreen(Lists.newArrayList(originLayers.get(currentLayer).getLeft()), 0, false));
			}));
			PlayerEntity player = MinecraftClient.getInstance().player;
			chooseOriginButton.active = chooseOriginButton.visible = originLayers.get(currentLayer).getRight() == Origin.EMPTY && originLayers.get(currentLayer).getLeft().getOriginOptionCount(player) > 0;
			if(originLayers.size() > 1) {
				addDrawableChild(new ButtonWidget(guiLeft - 40,this.height / 2 - 10, 20, 20, new LiteralText("<"), b -> {
					currentLayer = (currentLayer - 1 + originLayers.size()) % originLayers.size();
					Pair<OriginLayer, Origin> current = originLayers.get(currentLayer);
					showOrigin(current.getRight(), current.getLeft(), false);
					chooseOriginButton.active = chooseOriginButton.visible = current.getRight() == Origin.EMPTY && current.getLeft().getOriginOptionCount(player) > 0;
				}));
				addDrawableChild(new ButtonWidget(guiLeft + windowWidth + 20, this.height / 2 - 10, 20, 20, new LiteralText(">"), b -> {
					currentLayer = (currentLayer + 1) % originLayers.size();
					Pair<OriginLayer, Origin> current = originLayers.get(currentLayer);
					showOrigin(current.getRight(), current.getLeft(), false);
					chooseOriginButton.active = chooseOriginButton.visible = current.getRight() == Origin.EMPTY && current.getLeft().getOriginOptionCount(player) > 0;
				}));
			}
		}
		addDrawableChild(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, new TranslatableText(Origins.MODID + ".gui.close"), b -> {
			MinecraftClient.getInstance().setScreen(null);
        }));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		if(originLayers.size() == 0) {
			if(OriginsClient.isServerRunningOrigins) {
				drawCenteredText(matrices, this.textRenderer, new TranslatableText(Origins.MODID + ".gui.view_origin.empty").getString(), width / 2, guiTop + 48, 0xFFFFFF);
			} else {
				drawCenteredText(matrices, this.textRenderer, new TranslatableText(Origins.MODID + ".gui.view_origin.not_installed").getString(), width / 2, guiTop + 48, 0xFFFFFF);
			}
		}
	}

	@Override
	protected Text getTitleText() {
		if (getCurrentLayer().shouldOverrideViewOriginTitle()) {
			return new TranslatableText(getCurrentLayer().getTitleViewOriginTranslationKey());
		}
		return new TranslatableText(Origins.MODID + ".gui.view_origin.title", new TranslatableText(getCurrentLayer().getTranslationKey()));
	}

}

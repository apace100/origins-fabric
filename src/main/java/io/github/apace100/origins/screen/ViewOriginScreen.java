package io.github.apace100.origins.screen;

import com.google.common.collect.Lists;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class ViewOriginScreen extends OriginDisplayScreen {

	private final ArrayList<Pair<OriginLayer, Origin>> originLayers;
	private ButtonWidget chooseOriginButton;

	private int currentLayerIndex = 0;

	public ViewOriginScreen() {
		super(Text.translatable(Origins.MODID + ".screen.view_origin"), false);

		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			originLayers = new ArrayList<>(5);
			return;
		}

		Map<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(player).getOrigins();
		originLayers = new ArrayList<>(origins.size());

		origins.forEach((layer, origin) -> {

			ItemStack iconStack = origin.getDisplayItem();
			if (iconStack.isOf(Items.PLAYER_HEAD) && (!iconStack.hasNbt() || !iconStack.getOrCreateNbt().contains("SkullOwner"))) {
				iconStack.getOrCreateNbt().putString("SkullOwner", player.getName().getString());
			}

			if (!layer.isHidden() || (origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0)) {
				originLayers.add(new Pair<>(layer, origin));
			}

		});

		originLayers.sort(Comparator.comparing(Pair::getLeft));
		if (originLayers.isEmpty()) {
			showOrigin(null, null, false);
		} else {
			Pair<OriginLayer, Origin> currentOriginAndLayer = originLayers.get(currentLayerIndex);
			showOrigin(currentOriginAndLayer.getRight(), currentOriginAndLayer.getLeft(), false);
		}

	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	protected void init() {

		super.init();
		MinecraftClient client = MinecraftClient.getInstance();

		addDrawableChild(ButtonWidget.builder(
			Text.translatable(Origins.MODID + ".gui.close"),
			button -> client.setScreen(null)
		).dimensions(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT + 5, 100, 20).build());

		if (originLayers.isEmpty() || !OriginsClient.isServerRunningOrigins) {
			return;
		}

		addDrawableChild(chooseOriginButton = ButtonWidget.builder(
			Text.translatable(Origins.MODID + ".gui.choose"),
			button -> client.setScreen(new ChooseOriginScreen(Lists.newArrayList(getCurrentLayer()), 0, false))
		).dimensions(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT - 40, 100, 20).build());

		PlayerEntity player = client.player;
		chooseOriginButton.active = chooseOriginButton.visible = getCurrentOrigin() == Origin.EMPTY && getCurrentLayer().getOriginOptionCount(player) > 0;

		if (originLayers.size() <= 1) {
			return;
		}

		//	Draw previous layer button
		addDrawableChild(ButtonWidget.builder(
			Text.of("<"),
			button -> {

				currentLayerIndex = (currentLayerIndex - 1 + originLayers.size()) % originLayers.size();
				showOrigin(getCurrentOrigin(), getCurrentLayer(), false);

				chooseOriginButton.active = chooseOriginButton.visible = getCurrentOrigin() == Origin.EMPTY && getCurrentLayer().getOriginOptionCount(player) > 0;

			}
		).dimensions(guiLeft - 40, height / 2 - 10, 20, 20).build());

		//	Draw next layer button
		addDrawableChild(ButtonWidget.builder(
			Text.of(">"),
			button -> {

				currentLayerIndex = (currentLayerIndex + 1) % originLayers.size();
				showOrigin(getCurrentOrigin(), getCurrentLayer(), false);

				chooseOriginButton.active = chooseOriginButton.visible = getCurrentOrigin() == Origin.EMPTY && getCurrentLayer().getOriginOptionCount(player) > 0;

			}
		).dimensions(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10, 20, 20).build());

	}

	@Override
	public OriginLayer getCurrentLayer() {
		return originLayers.get(currentLayerIndex).getLeft();
	}

	@Override
	public Origin getCurrentOrigin() {
		return originLayers.get(currentLayerIndex).getRight();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);
		if (!originLayers.isEmpty()) {
			return;
		}

		String translationKey = Origins.MODID + ".gui.view_origin." + (OriginsClient.isServerRunningOrigins ? "empty" : "not_installed");
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable(translationKey), width / 2, guiTop + 48, 0xFFFFFF);

	}

	@Override
	protected Text getTitleText() {

		OriginLayer currentLayer = super.getCurrentLayer();
		if (currentLayer.shouldOverrideViewOriginTitle()) {
			return Text.translatable(currentLayer.getTitleViewOriginTranslationKey());
		}

		return Text.translatable(Origins.MODID + ".gui.view_origin.title", Text.translatable(currentLayer.getTranslationKey()));

	}

}

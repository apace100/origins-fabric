package io.github.apace100.origins.screen;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.origin.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;

public class ChooseOriginScreen extends OriginDisplayScreen {

	private final List<OriginLayer> layers;
	private final int layerIndex;

	private final List<Origin> origins = new ObjectArrayList<>();
	private int originIndex = 0;

	private int optionCount;

	protected ChooseOriginScreen(List<OriginLayer> layers, int layerIndex, boolean showDirtBackground) {
		super(Text.translatable(Origins.MODID + ".screen.choose_origin"), showDirtBackground);
		this.layers = layers;
		this.layerIndex = MathHelper.clamp(Math.abs(layerIndex), 0, layers.size());
	}

	public ChooseOriginScreen(List<OriginLayer> layers, boolean showDirtBackground) {
		this(layers, 0, showDirtBackground);
	}

	public ChooseOriginScreen(OriginLayer layer, boolean showDirtBackground) {
		this(ObjectArrayList.of(layer), 0, showDirtBackground);
	}

	@Override
	protected void init() {

		super.init();
		assert client != null && client.player != null : "Tried initializing the choose origin screen with the client and its player unset!";

		if (layers.isEmpty()) {
			waitForNextLayer();
			return;
		}

		OriginLayer currentLayer = getCurrentLayer();
		this.initRandomDescription();

		this.optionCount = currentLayer.getOriginOptionCount(client.player);
		this.origins.clear();

		currentLayer.getOrigins(client.player).forEach(id -> {

			Origin origin = OriginManager.get(id);
			ItemStack icon = origin.getDisplayItem();

			if (origin.isChoosable()) {

				if (icon.getItem() instanceof PlayerHeadItem && !icon.contains(DataComponentTypes.PROFILE)) {
					icon.set(DataComponentTypes.PROFILE, new ProfileComponent(client.player.getGameProfile()));
				}

				origins.add(origin);

			}

		});

		this.origins.sort(Origin::compareTo);

		//  Manually add the random origin
		if (currentLayer.isRandomAllowed()) {
			origins.add(Origin.RANDOM);
		}

		if (optionCount == 0) {
			waitForNextLayer();
			return;
		}

		//	Draw the select origin button
		addDrawableChild(ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.select"), button -> this.selectOrigin())
			.position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT + 5)
			.size(100, 20)
			.build());

		showOrigin(getCurrentOrigin(), currentLayer);

		if (optionCount <= 1) {
			return;
		}

		addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.showPreviousOrigin())
			.position(guiLeft - 40, height / 2 - 10)
			.size(20, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.showNextOrigin())
			.position(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10)
			.size(20, 20)
			.build());

	}

	@Override
	protected Text getTitleText() {
		return super.getCurrentLayer().getChooseOriginTitle();
	}

	@Override
	public Origin getCurrentOrigin() {
		return origins.get(originIndex);
	}

	@Override
	public OriginLayer getCurrentLayer() {
		return layers.get(layerIndex);
	}

	void waitForNextLayer() {
		Objects.requireNonNull(client).setScreen(new WaitForNextLayerScreen(layers, layerIndex, showDirtBackground));
	}

	void selectOrigin() {

		Origin origin = getCurrentOrigin();
		OriginLayer layer = getCurrentLayer();

		if (origin == Origin.RANDOM) {
			ClientPlayNetworking.send(new ChooseRandomOriginC2SPacket(layer.getId()));
		}

		else {
			ClientPlayNetworking.send(new ChooseOriginC2SPacket(layer.getId(), origin.getId()));
		}

		waitForNextLayer();

	}

	void showNextOrigin() {

		this.originIndex = (originIndex + 1) % optionCount;
		var origin = getCurrentOrigin();

		showOrigin(origin, getCurrentLayer());

	}

	void showPreviousOrigin() {

		this.originIndex = Math.abs(originIndex - 1) % optionCount;
		var origin = getCurrentOrigin();

		showOrigin(origin, getCurrentLayer());

	}

}

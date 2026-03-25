package io.github.apace100.origins.screen;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.origin.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class ChooseOriginScreen extends OriginDisplayScreen {

	private final List<OriginLayer> layers;
	private final int layerIndex;

	private final List<Origin> origins = new ObjectArrayList<>();
	private int originIndex = 0;

	private int optionCount = 0;

	protected ChooseOriginScreen(List<OriginLayer> layers, int layerIndex, boolean showDirtBackground) {
		super(Text.translatable(Origins.MODID + ".screen.choose_origin"), showDirtBackground);
		this.layers = layers;
		this.layerIndex = layerIndex;
	}

	public ChooseOriginScreen(List<OriginLayer> layers, boolean showDirtBackground) {
		this(layers, 0, showDirtBackground);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {

		super.init();
		assert client != null && client.player != null : "Tried initializing the choose origin screen with the client and its player unset!";

		this.initRandomDescription();
		this.origins.clear();

		OriginLayer currentLayer = getCurrentLayer();
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
		this.optionCount = currentLayer.getOriginOptionCount(client.player);

		//  Manually add the random origin
		if (currentLayer.isRandomAllowed()) {
			origins.add(Origin.RANDOM);
		}

		if (optionCount == 0) {
			nextLayer();
		}

		//	Draw the select origin button
		addDrawableChild(ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.select"), button -> {

				Origin origin = getCurrentOrigin();
				OriginLayer layer = getCurrentLayer();

				if (origin == Origin.RANDOM) {
					ClientPlayNetworking.send(new ChooseRandomOriginC2SPacket(layer.getId()));
				}

				else {
					ClientPlayNetworking.send(new ChooseOriginC2SPacket(layer.getId(), origin.getId()));
				}

				nextLayer();

			})
			.position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT + 5)
			.size(100, 20)
			.build());

		showOrigin(getCurrentOrigin(), getCurrentLayer());

		if (optionCount <= 1) {
			return;
		}

		//	Draw the previous origin button
		addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> {
				originIndex = ((originIndex - 1) + optionCount) % optionCount;
				showOrigin(getCurrentOrigin(), getCurrentLayer());
			})
			.position(guiLeft - 40, height / 2 - 10)
			.size(20, 20)
			.build());

//		//	Draw the next origin button
		addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> {
				originIndex = (originIndex + 1) % optionCount;
				showOrigin(getCurrentOrigin(), getCurrentLayer());
			})
			.position(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10)
			.size(20, 20)
			.build());

	}

	@Override
	public OriginLayer getCurrentLayer() {
		return layers.get(layerIndex);
	}

	@Override
	public Origin getCurrentOrigin() {
		return origins.get(originIndex);
	}

	@Override
	protected Text getTitleText() {
		return super.getCurrentLayer().getChooseOriginTitle();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		if (optionCount == 0) {
			nextLayer();
		}

		else {
			super.render(context, mouseX, mouseY, delta);
		}

	}

	public void nextLayer() {
		Objects.requireNonNull(client).setScreen(new WaitForNextLayerScreen(layers, layerIndex, showDirtBackground));
	}

}

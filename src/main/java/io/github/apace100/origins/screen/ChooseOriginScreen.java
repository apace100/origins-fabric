package io.github.apace100.origins.screen;

import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginManager;
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
		super(Text.empty(), showDirtBackground);
		this.layers = layers;
		this.layerIndex = MathHelper.clamp(Math.abs(layerIndex), 0, layers.size());
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

		this.optionCount = 0;
		this.origins.clear();

		if (layers.isEmpty()) {
			this.waitForNextLayer();
		}

		else {

			OriginLayer currentLayer = getCurrentLayer();
			this.optionCount = currentLayer.getOriginOptionCount(client.player);

			for (var originId : currentLayer.getOrigins(client.player)) {

				Origin origin = OriginManager.get(originId);
				ItemStack icon = origin.getDisplayItem();

				if (origin.isChoosable()) {

					if (icon.getItem() instanceof PlayerHeadItem && !icon.contains(DataComponentTypes.PROFILE)) {
						icon.set(DataComponentTypes.PROFILE, new ProfileComponent(client.player.getGameProfile()));
					}

					origins.add(origin);

				}

			}

			this.origins.sort(Origin::compareTo);

			//  Manually add the random origin if random is allowed
			if (currentLayer.isRandomAllowed()) {
				origins.add(Origin.RANDOM);
			}

			if (optionCount == 0) {
				this.waitForNextLayer();
			}

			else {

				this.addDrawableChild(ButtonWidget.builder(Text.translatable("origins.gui.select"), button -> this.selectOrigin())
					.position(windowWidget.getX() + windowWidget.getWidth() / 2 - 50, windowWidget.getY() + windowWidget.getHeight() + 5)
					.size(100, 20)
					.build());

				this.showCurrent();

				if (optionCount > 1) {

					this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> this.previousOrigin())
						.position(windowWidget.getX() - 40, height / 2 - 10)
						.size(20, 20)
						.build());
					this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> this.nextOrigin())
						.position(windowWidget.getX() + windowWidget.getWidth() + 20, height / 2 - 10)
						.size(20, 20)
						.build());

				}

			}

		}

	}

	@Override
	public Text getTitle() {

		if (layers.isEmpty()) {
			return super.getTitle();
		}

		else {
			return this.getCurrentLayer().getChooseOriginTitle();
		}

	}

	@Override
	protected Origin getCurrentOrigin() {
		return origins.get(originIndex);
	}

	@Override
	protected OriginLayer getCurrentLayer() {
		return layers.get(layerIndex);
	}

	protected void waitForNextLayer() {
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

	void showCurrent() {
		this.showCurrent(origin -> origin.getGuiMetadata().choosing());
	}

	void resetAndShowCurrent() {
		this.resetAndShowCurrent(origin -> origin.getGuiMetadata().choosing());
	}

	void nextOrigin() {
		this.originIndex = MathHelper.floorMod(originIndex + 1, optionCount);
		this.resetAndShowCurrent();
	}

	void previousOrigin() {
		this.originIndex = MathHelper.floorMod(originIndex - 1, optionCount);
		this.resetAndShowCurrent();
	}

}

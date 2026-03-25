package io.github.apace100.origins.screen;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ViewOriginScreen extends OriginDisplayScreen {

	private final List<Pair<OriginLayer, Origin>> layers = new ObjectArrayList<>();
	private int index = 0;

	public ViewOriginScreen() {
		super(Text.translatable(Origins.MODID + ".screen.view_origin"), false);
	}

    @Override
	protected void init() {

		super.init();
		assert client != null && client.player != null;

		Map<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(client.player).getOrigins();
		this.layers.clear();

		origins.forEach((layer, origin) -> {

			ItemStack icon = origin.getDisplayItem();
			boolean hidden = layer.isHidden();

			if (icon.getItem() instanceof PlayerHeadItem && !icon.contains(DataComponentTypes.PROFILE)) {
				icon.set(DataComponentTypes.PROFILE, new ProfileComponent(client.player.getGameProfile()));
			}

			if (!hidden && (origin != Origin.EMPTY || layer.getOriginOptionCount(client.player) > 0)) {
				this.layers.add(new Pair<>(layer, origin));
			}

		});

	    //  Add the close button
//	    addDrawableChild(ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.close"), button -> client.setScreen(null))
//		    .position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT)
//		    .size(100, 20)
//		    .build()
//	    );

		if (this.layers.isEmpty() || !OriginsClient.isServerRunningOrigins) {
			return;
		}

	    try {

		    this.layers.sort(Comparator.comparing(Pair::getLeft));
		    Pair<OriginLayer, Origin> current = getCurrent();

		    showOrigin(current.getRight(), current.getLeft());

	    }

	    catch (IndexOutOfBoundsException e) {
		    showOrigin(null, null);
	    }

		//  Add the choose button
	    var chooseButton = ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.choose"), button -> client.setScreen(new ChooseOriginScreen(ObjectArrayList.of(getCurrentLayer()), false)))
		    .position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT)
		    .size(100, 20)
		    .build();

		chooseButton.visible = getCurrentOrigin() == Origin.EMPTY && getCurrentLayer().getOriginOptionCount(client.player) > 0;
		addDrawableChild(chooseButton);

	    var closeButton = ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.close"), button -> client.setScreen(null))
		    .position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT)
		    .size(100, 20)
		    .build();

	    closeButton.visible = !chooseButton.visible;
		addDrawableChild(closeButton);

		if (this.layers.size() <= 1) {
			return;
		}

		//  Add the previous button
		addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> {

				int layersCount = this.layers.size();
				index = ((index - 1) + layersCount) % layersCount;

				var currentLayer = getCurrentLayer();
				showOrigin(getCurrentOrigin(), currentLayer);

				chooseButton.visible = getCurrentOrigin() == Origin.EMPTY && currentLayer.getOriginOptionCount(client.player) > 0;

			})
			.position(guiLeft - 40, height / 2 - 10)
			.size(20, 20)
			.build()
		);

		//  Add the next button
		addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> {

				index = (index + 1) % this.layers.size();
				var currentLayer = getCurrentLayer();

				showOrigin(getCurrentOrigin(), currentLayer);
				chooseButton.visible = getCurrentOrigin() == Origin.EMPTY && currentLayer.getOriginOptionCount(client.player) > 0;

			})
			.position(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10)
			.size(20, 20)
			.build()
		);

	}

	@Override
	public OriginLayer getCurrentLayer() {
		return getCurrent().getLeft();
	}

	@Override
	public Origin getCurrentOrigin() {
		return getCurrent().getRight();
	}

	public Pair<OriginLayer, Origin> getCurrent() {
		return layers.get(index);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);
		if (!layers.isEmpty()) {
			return;
		}

		String translationKey = Origins.MODID + ".gui.view_origin." + (OriginsClient.isServerRunningOrigins ? "empty" : "not_installed");
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable(translationKey), width / 2, guiTop + 48, 0xFFFFFF);

	}

	@Override
	protected Text getTitleText() {
		return super.getCurrentLayer().getViewOriginTitle();
	}

}

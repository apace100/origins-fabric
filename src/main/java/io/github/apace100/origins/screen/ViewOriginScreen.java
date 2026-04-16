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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ViewOriginScreen extends OriginDisplayScreen {

	private final List<Entry> entries = new ObjectArrayList<>();
	private int index = 0;

	public ViewOriginScreen() {
		super(Text.translatable(Origins.MODID + ".screen.view_origin"), false);
	}

    @Override
	protected void init() {

		super.init();
		assert client != null && client.player != null;

		Map<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(client.player).getOrigins();
		this.entries.clear();

		origins.forEach((layer, origin) -> {

			if (!layer.isEnabled() || layer.isHidden() || (origin == Origin.EMPTY && layer.getOriginOptionCount(client.player) <= 0)) {
				return;
			}

			this.entries.add(new Entry(layer, origin));
			ItemStack icon = origin.getDisplayItem();

			if (icon.getItem() instanceof PlayerHeadItem && !icon.contains(DataComponentTypes.PROFILE)) {
				icon.set(DataComponentTypes.PROFILE, new ProfileComponent(client.player.getGameProfile()));
			}

		});

		addDrawableChild(ButtonWidget.builder(Text.translatable(Origins.MODID + ".gui.close"), button -> client.setScreen(null))
			.position(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT + 5)
			.size(100, 20)
			.build());

		if (this.entries.isEmpty() || !OriginsClient.isServerRunningOrigins) {
			return;
		}

		this.entries.sort(Entry::compareTo);
		showOrigin(getCurrentOrigin(), getCurrentLayer());

		if (this.entries.size() <= 1) {
			return;
		}

		addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.showPrevious())
			.position(guiLeft - 40, height / 2 - 10)
			.size(20, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.showNext())
			.position(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10)
			.size(20, 20)
			.build());

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (entries.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable(Origins.MODID + ".gui.view_origin." + (OriginsClient.isServerRunningOrigins ? "empty" : "not_installed")), width / 2, guiTop + 48, 0xFFFFFF);
		}

	}

	@Override
	protected Text getTitleText() {
		return super.getCurrentLayer().getViewOriginTitle();
	}

	@Override
	public Origin getCurrentOrigin() {
		return getCurrent().origin();
	}

	@Override
	public OriginLayer getCurrentLayer() {
		return getCurrent().layer();
	}

	protected Entry getCurrent() {
		return entries.get(index);
	}

	protected void showNext() {

		this.index = (index + 1) % this.entries.size();
		var entry = this.entries.get(this.index);

		showOrigin(entry.origin(), entry.layer());

	}

	protected void showPrevious() {

		this.index = Math.abs(this.index - 1) % this.entries.size();
		var entry = this.entries.get(this.index);

		showOrigin(entry.origin(), entry.layer());

	}

	public record Entry(OriginLayer layer, Origin origin) implements Comparable<Entry> {

		@Override
		public int compareTo(@NotNull Entry that) {
			return this.layer().compareTo(that.layer());
		}

	}

}

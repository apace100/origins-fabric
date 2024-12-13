package io.github.apace100.origins.component.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.registry.ModComponents;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class ItemOriginsComponent implements TooltipAppender {

    public static final ItemOriginsComponent DEFAULT = new ItemOriginsComponent(new ObjectLinkedOpenHashSet<>());

    public static final Codec<ItemOriginsComponent> CODEC = Entry.SET_CODEC.xmap(
        ItemOriginsComponent::new,
        ItemOriginsComponent::entries
    );

    public static final PacketCodec<PacketByteBuf, ItemOriginsComponent> PACKET_CODEC = PacketCodecs.collection(ObjectLinkedOpenHashSet::new, Entry.PACKET_CODEC).xmap(
        ItemOriginsComponent::new,
		ItemOriginsComponent::entries
    );

    final ObjectLinkedOpenHashSet<Entry> entries;

    ItemOriginsComponent(Collection<Entry> entries) {
        this.entries = new ObjectLinkedOpenHashSet<>(entries);
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

        String baseKey = "component.item.origins.origin";
        boolean appendedTooltips = false;

		for (Entry entry : entries()) {

			OriginLayer layer = OriginLayerManager.getNullable(entry.layerId());
			Origin origin = OriginManager.getNullable(entry.originId());

			if (!canSet(layer, origin)) {
				continue;
			}

			String translationKey;
			Object[] args;

			if (origin == Origin.EMPTY) {
				translationKey = baseKey + ".layer";
				args = new Object[]{layer.getName()};
			}

            else {
				translationKey = baseKey + ".layer_and_origin";
				args = new Object[]{layer.getName(), origin.getName()};
			}

			tooltip.accept(Text.translatable(translationKey, args).formatted(Formatting.GRAY));
			appendedTooltips = true;

		}

        if (!appendedTooltips) {
            tooltip.accept(Text.translatable(baseKey + ".generic").formatted(Formatting.GRAY));
        }

    }

    private ObjectLinkedOpenHashSet<Entry> entries() {
        return entries;
    }

    public void setOrigin(LivingEntity user) {

        if (!(user instanceof ServerPlayerEntity player)) {
            return;
        }

		OriginComponent originComponent = ModComponents.ORIGIN.get(player);
		boolean assignedOrigin = false;

		for (Entry entry : entries()) {

			OriginLayer layer = OriginLayerManager.getNullable(entry.layerId());
			Origin origin = OriginManager.getNullable(entry.originId());

			if (canSet(layer, origin)) {
				originComponent.setOrigin(layer, origin);
				assignedOrigin = true;
			}

		}

        assignedOrigin |= originComponent.checkAutoChoosingLayers(player, false);
        int originOptions = OriginLayerManager.getOriginOptionCount(player);

        originComponent.selectingOrigin(!assignedOrigin || originOptions > 0);
        originComponent.sync();

        if (originComponent.isSelectingOrigin()) {
            ServerPlayNetworking.send(player, new OpenChooseOriginScreenS2CPacket(false));
        }

    }

    public record Entry(Identifier layerId, Identifier originId) {

        public static final MapCodec<Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("layer").forGetter(Entry::layerId),
            Identifier.CODEC.optionalFieldOf("origin", Origin.EMPTY.getId()).forGetter(Entry::originId)
        ).apply(instance, Entry::new));

        public static final PacketCodec<PacketByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, Entry::layerId,
            Identifier.PACKET_CODEC, Entry::originId,
            Entry::new
        );

        public static final Codec<Set<Entry>> SET_CODEC = MAP_CODEC.codec().listOf().xmap(
            ImmutableSet::copyOf,
            ImmutableList::copyOf
        );

    }

	private static boolean canSet(@Nullable OriginLayer layer, @Nullable Origin origin) {
		return layer != null
			&& origin != null
			&& layer.isEnabled()
			&& (layer.contains(origin) || origin.isSpecial());
	}

}

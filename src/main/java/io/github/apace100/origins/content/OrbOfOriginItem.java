package io.github.apace100.origins.content;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrbOfOriginItem extends Item {

    public OrbOfOriginItem() {
        this(new Settings().maxCount(1).rarity(Rarity.RARE));
    }

    public OrbOfOriginItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);
        if(!world.isClient) {

            OriginComponent component = ModComponents.ORIGIN.get(user);
            Map<OriginLayer, Origin> targets = getTargets(stack);
            if(!targets.isEmpty()) {
                for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
                    component.setOrigin(target.getKey(), target.getValue());
                }
            } else {
                for (OriginLayer layer : OriginLayers.getLayers()) {
                    if(layer.isEnabled()) {
                        component.setOrigin(layer, Origin.EMPTY);
                    }
                }
            }

            boolean originAutomaticallyAssigned = component.checkAutoChoosingLayers(user, false);
            int originOptions = OriginLayers.getOriginOptionCount(user);

            component.selectingOrigin(!originAutomaticallyAssigned || originOptions > 0);
            component.sync();

            if (component.isSelectingOrigin()) {
                ServerPlayNetworking.send((ServerPlayerEntity) user, new OpenChooseOriginScreenS2CPacket(false));
            }

        }

        if(!user.isCreative()) {
            stack.decrement(1);
        }

        return TypedActionResult.consume(stack);

    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        getTargets(stack).forEach((originLayer, origin) -> {

            String baseKey = "item.origins.orb_of_origin.layer_";
            Object[] args;

            if (origin == Origin.EMPTY) {
                baseKey += "generic";
                args = new Object[] { Text.translatable(originLayer.getTranslationKey()) };
            } else {
                baseKey += "specific";
                args = new Object[] {
                    Text.translatable(originLayer.getTranslationKey()),
                    Text.translatable(origin.getOrCreateNameTranslationKey())
                };
            }

            tooltip.add(Text.translatable(baseKey, args));

        });

    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {

        Map<OriginLayer, Origin> targets = new HashMap<>();

        NbtCompound stackNbt = stack.getNbt();
        NbtList targetsNbt = stackNbt == null ? new NbtList() : stackNbt.getList("Targets", NbtElement.COMPOUND_TYPE);

        if (targetsNbt.isEmpty()) {
            return targets;
        }

        for (NbtElement nbtElement : targetsNbt) {
            try {

                NbtCompound targetNbt = (NbtCompound) nbtElement;
                Identifier layerId = new Identifier(targetNbt.getString("Layer"));

                OriginLayer layer = OriginLayers.getLayer(layerId);
                Origin origin = Origin.EMPTY;

                if (targetNbt.contains("Origin", NbtElement.STRING_TYPE)) {
                    Identifier originId = new Identifier(targetNbt.getString("Origin"));
                    origin = OriginRegistry.get(originId);
                }

                if (layer.isEnabled() && (layer.contains(origin) || origin.isSpecial())) {
                    targets.put(layer, origin);
                }

            } catch (Exception ignored) {

            }
        }

        return targets;

    }
}

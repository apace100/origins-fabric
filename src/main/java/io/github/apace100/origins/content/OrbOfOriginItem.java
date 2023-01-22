package io.github.apace100.origins.content;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
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
        super(new Item.Settings().maxCount(1).rarity(Rarity.RARE));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(!world.isClient) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            Map<OriginLayer, Origin> targets = getTargets(stack);
            if(targets.size() > 0) {
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
            component.checkAutoChoosingLayers(user, false);
            component.sync();
            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeBoolean(false);
            ServerPlayNetworking.send((ServerPlayerEntity) user, ModPackets.OPEN_ORIGIN_SCREEN, data);
        }
        if(!user.isCreative()) {
            stack.decrement(1);
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Map<OriginLayer, Origin> targets = getTargets(stack);
        for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
            if(target.getValue() == Origin.EMPTY) {
                tooltip.add(Text.translatable("item.origins.orb_of_origin.layer_generic",
                    Text.translatable(target.getKey().getTranslationKey())).formatted(Formatting.GRAY));
            } else {
                tooltip.add(Text.translatable("item.origins.orb_of_origin.layer_specific",
                    Text.translatable(target.getKey().getTranslationKey()),
                    target.getValue().getName()).formatted(Formatting.GRAY));
            }
        }
    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {
        HashMap<OriginLayer, Origin> targets = new HashMap<>();
        if(!stack.hasNbt()) {
            return targets;
        }
        NbtCompound nbt = stack.getNbt();
        if(!nbt.contains("Targets", NbtType.LIST)) {
            return targets;
        }
        NbtList targetList = (NbtList)nbt.get("Targets");
        for (NbtElement nbtElement : targetList) {
            if(nbtElement instanceof NbtCompound targetNbt) {
                if(targetNbt.contains("Layer", NbtType.STRING)) {
                    try {
                        Identifier id = new Identifier(targetNbt.getString("Layer"));
                        OriginLayer layer = OriginLayers.getLayer(id);
                        Origin origin = Origin.EMPTY;
                        if(targetNbt.contains("Origin", NbtType.STRING)) {
                            Identifier originId = new Identifier(targetNbt.getString("Origin"));
                            origin = OriginRegistry.get(originId);
                        }
                        if(layer.isEnabled() && (layer.contains(origin) || origin.isSpecial())) {
                            targets.put(layer, origin);
                        }
                    } catch (Exception e) {
                        // no op
                    }
                }
            }
        }
        return targets;
    }
}

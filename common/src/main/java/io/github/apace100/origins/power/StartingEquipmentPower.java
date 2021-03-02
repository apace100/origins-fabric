package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StartingEquipmentPower extends Power {

    private final List<ItemStack> itemStacks = new LinkedList<>();
    private final HashMap<Integer, ItemStack> slottedStacks = new HashMap<>();
    private boolean recurrent;

    public StartingEquipmentPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void addStack(ItemStack stack) {
        this.itemStacks.add(stack);
    }

    public void addStack(int slot, ItemStack stack) {
        slottedStacks.put(slot, stack);
    }

    @Override
    public void onChosen(boolean isOrbOfOrigin) {
        giveStacks();
    }

    @Override
    public void onRespawn() {
        if(recurrent) {
            giveStacks();
        }
    }

    private void giveStacks() {
        slottedStacks.forEach((slot, stack) -> {
            Origins.LOGGER.info("Giving player " + stack.toString());
            if(player.inventory.getStack(slot).isEmpty()) {
                player.inventory.setStack(slot, stack);
            } else {
                player.giveItemStack(stack);
            }
        });
        itemStacks.forEach(is -> {
            ItemStack copy = is.copy();
            Origins.LOGGER.info("Giving player " + copy.toString());
            player.giveItemStack(copy);
        });
    }
}

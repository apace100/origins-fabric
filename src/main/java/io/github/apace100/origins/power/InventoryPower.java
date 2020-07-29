package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

public class InventoryPower extends Power implements Active, Inventory {

    private static final int SIZE = 3 * 3;
    private DefaultedList<ItemStack> inventory;
    private TranslatableText containerName;

    public InventoryPower(PowerType<?> type, PlayerEntity player, String containerName) {
        super(type, player);
        this.inventory = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        this.containerName = new TranslatableText(containerName);
    }

    @Override
    public void onUse() {
        if(!player.world.isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
                return new Generic3x3ContainerScreenHandler(i, playerInventory, this);
            }, containerName));
        }
    }

    @Override
    public Tag toTag() {
        CompoundTag tag = new CompoundTag();
        Inventories.toTag(tag, inventory);
        return tag;
    }

    @Override
    public void fromTag(Tag tag) {
        Inventories.fromTag((CompoundTag)tag, inventory);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = inventory.get(slot);
        setStack(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void clear() {
        for(int i = 0; i < SIZE; i++) {
            setStack(i, ItemStack.EMPTY);
        }
    }
}

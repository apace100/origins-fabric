package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Predicate;

public class InventoryPower extends Power implements Active, Inventory {

    private final int size;
    private final DefaultedList<ItemStack> inventory;
    private final TranslatableText containerName;
    private final ScreenHandlerFactory factory;
    private final boolean shouldDropOnDeath;
    private final Predicate<ItemStack> dropOnDeathFilter;

    public InventoryPower(PowerType<?> type, PlayerEntity player, String containerName, int size, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter) {
        super(type, player);
        this.size = size;
        this.inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.containerName = new TranslatableText(containerName);
        this.factory = (i, playerInventory, playerEntity) -> {
            return new Generic3x3ContainerScreenHandler(i, playerInventory, this);
        };
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
    }

    @Override
    public void onUse() {
        if(!player.world.isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(factory, containerName));
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
        return size;
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
        for(int i = 0; i < size; i++) {
            setStack(i, ItemStack.EMPTY);
        }
    }

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }
}

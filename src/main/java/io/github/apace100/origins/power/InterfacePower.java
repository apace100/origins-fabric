package io.github.apace100.origins.power;

import io.github.apace100.origins.screen.ContainerScreenHandler;
import io.github.apace100.origins.util.CustomAnvil;
import io.github.apace100.origins.util.CustomCraftingTable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.*;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class InterfacePower extends Power implements Active, Inventory {

    private final int size;
    private final int rows;
    private final DefaultedList<ItemStack> inventory;
    private final TranslatableText containerName;
    private final String interfaceType;
    private final ScreenHandlerFactory factory;
    private final boolean shouldDropOnDeath;
    private final Predicate<ItemStack> dropOnDeathFilter;

    public InterfacePower(PowerType<?> type, PlayerEntity player, String containerName, String interfaceType, int size, int rows, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter) {
        super(type, player);
        size = size * rows;
        this.size = size;
        this.rows = rows;
        this.interfaceType = interfaceType;
        this.inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.containerName = new TranslatableText(containerName);
        if(this.interfaceType.equals("minecraft:chest")) {
            switch (rows) {
                case 2:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, i, playerInventory, this, rows);
                    };
                    break;
                case 3:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, playerInventory, this, rows);
                    };
                    break;
                case 4:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, i, playerInventory, this, rows);
                    };
                    break;
                case 5:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, i, playerInventory, this, rows);
                    };
                    break;
                case 6:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, i, playerInventory, this, rows);
                    };
                    break;
                default:
                    this.factory = (i, playerInventory, playerEntity) -> {
                        return new ContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, i, playerInventory, this, rows);
                    };
                    break;
            }
        } else if (this.interfaceType.equals("minecraft:crafting_table")) {
            this.factory = (i, playerInventory, playerEntity) -> {
                return new CustomCraftingTable(1, player.inventory,
                        ScreenHandlerContext.create(player.world, new BlockPos(0,0,0)));
            };
        } else if (this.interfaceType.equals("minecraft:hopper")) {
            this.factory = (i, playerInventory, playerEntity) -> {
                return new HopperScreenHandler(i, playerInventory, this);
            };
        } else if (this.interfaceType.equals("minecraft:ender_chest")) {
            this.factory = (i, playerInventory, playerEntity) -> {
                return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, player.getEnderChestInventory());
            };
        } else if (this.interfaceType.equals("minecraft:anvil")) {
            this.factory = (i, playerInventory, playerEntity) -> {
                return new CustomAnvil(i, playerInventory, ScreenHandlerContext.create(player.world, player.getBlockPos()));
            };
        } else if (this.interfaceType.equals("minecraft:dropper")) {
            this.factory = (i, playerInventory, playerEntity) -> {
                return new Generic3x3ContainerScreenHandler(i, playerInventory, this);
            };
        }

        else {
            this.factory = (i, playerInventory, playerEntity) -> {
                return new Generic3x3ContainerScreenHandler(i, playerInventory, this);
            };
        }
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

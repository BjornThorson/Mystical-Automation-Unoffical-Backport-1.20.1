package com.blakebr0.mysticalautomation.container;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.tileentity.FarmerBlockEntity;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** Forge Farmer GUI menu, using the original upstream 176x194 slot geometry. */
public final class FarmerMenu extends AbstractContainerMenu {
    private static final int PLAYER_START = 14;
    private static final int TOTAL = PLAYER_START + 36;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public FarmerMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, buffer.readBlockPos(), new ItemStackHandler(16),
                new UpgradeItemStackHandler(), new SimpleContainerData(11));
    }

    public FarmerMenu(int id, Inventory playerInventory, FarmerBlockEntity machine) {
        this(id, playerInventory, machine.getBlockPos(), machine.getInventory(),
                machine.getUpgradeInventory(), machine.getDisplayData());
    }

    private FarmerMenu(int id, Inventory playerInventory, BlockPos pos,
                       ItemStackHandler contents, UpgradeItemStackHandler upgrades, ContainerData data) {
        super(ModMenuTypes.FARMER.get(), id);
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), pos);
        this.data = data;

        addSlot(new SlotItemHandler(upgrades, 0, 152, 9) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof MachineUpgradeItem;
            }
        });
        for (int i = 0; i < 3; i++)
            addSlot(new SlotItemHandler(contents, i, 60, 30 + i * 22));
        addSlot(new SlotItemHandler(contents, FarmerBlockEntity.FUEL, 30, 56) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                addSlot(new SlotItemHandler(contents,
                        FarmerBlockEntity.FIRST_OUTPUT + row * 3 + col,
                        116 + col * 18, 34 + row * 18) {
                    @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
                });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInventory, 9 + row * 9 + col,
                        8 + col * 18, 112 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 170));
        addDataSlots(data);
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FARMER.get());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, TOTAL, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof MachineUpgradeItem) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (ForgeHooks.getBurnTime(stack, null) > 0) {
            if (!moveItemStackTo(stack, 4, 5, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 1, 4, false)) {
            if (index < PLAYER_START + 27) {
                if (!moveItemStackTo(stack, PLAYER_START + 27, TOTAL, false))
                    return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, PLAYER_START, PLAYER_START + 27, false))
                return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    private int pair(int lo, int hi) {
        return (data.get(lo) & 0xFFFF) | ((data.get(hi) & 0xFFFF) << 16);
    }
    public int getEnergy() { return pair(0, 1); }
    public int getMaximumEnergy() { return pair(2, 3); }
    public int getFuelLeft() { return data.get(4); }
    public int getFuelItemValue() { return data.get(5); }
    public int getProgress() { return data.get(6); }
    public int getOperationTicks() { return data.get(7); }
    public int getStages() { return data.get(8); }
    public int getStageProgress() { return data.get(9); }
    public int getMaximumStageProgress() { return data.get(10); }
}

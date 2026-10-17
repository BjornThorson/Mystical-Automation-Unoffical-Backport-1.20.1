package com.blakebr0.mysticalautomation.container;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalautomation.tileentity.InfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** Forge 1.20.1 menu layout based on the upstream InfuserContainer. */
public final class InfuserMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final InfuserBlockEntity machine;

    /** Client factory, coordinates sent with NetworkHooks.openScreen. */
    public InfuserMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, buffer.readBlockPos(), null,
                new ItemStackHandler(9), new UpgradeItemStackHandler(), new SimpleContainerData(14));
    }

    public InfuserMenu(int id, Inventory playerInventory, InfuserBlockEntity machine) {
        this(id, playerInventory, machine.getBlockPos(), machine,
                machine.getInventory(), machine.getUpgradeInventory(), machine.getDisplayData());
    }

    private InfuserMenu(int id, Inventory playerInventory, BlockPos pos, InfuserBlockEntity machine,
                        ItemStackHandler handler, UpgradeItemStackHandler upgrades, ContainerData data) {
        super(ModMenuTypes.INFUSER.get(), id);
        this.machine = machine;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), pos);
        this.data = data;

        this.addSlot(new SlotItemHandler(upgrades, 0, 192, 9) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof MachineUpgradeItem;
            }
        });
        this.addSlot(machineSlot(handler, 0, 62, 33));
        for (int i = 0; i < 5; i++) this.addSlot(essenceSlot(handler, i + 1, 102 + i * 18, 33));
        this.addSlot(essenceSlot(handler, 6, 192, 33));
        this.addSlot(machineSlot(handler, 7, 30, 56));
        this.addSlot(machineSlot(handler, 8, 162, 74));

        for (int row = 0; row < 3; row++)
            for (int column = 0; column < 9; column++)
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        29 + column * 18, 112 + row * 18));
        for (int column = 0; column < 9; column++)
            this.addSlot(new Slot(playerInventory, column, 29 + column * 18, 170));

        this.addDataSlots(data);
    }

    /** Network-safe preview of the server's 512-item backing inventory. */
    private SlotItemHandler essenceSlot(ItemStackHandler handler, int backingSlot, int x, int y) {
        return new SlotItemHandler(handler, backingSlot, x, y) {
            @Override public @NotNull ItemStack getItem() {
                ItemStack actual = handler.getStackInSlot(backingSlot);
                if (actual.isEmpty()) return ItemStack.EMPTY;
                ItemStack safe = actual.copy();
                safe.setCount(1);
                return safe;
            }
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return InfuserBlockEntity.essenceTier(stack) == backingSlot - 1;
            }
            @Override public int getMaxStackSize(ItemStack stack) { return 64; }
        };
    }

    public int getEssenceCount(int tierIndex) {
        if (tierIndex < 0 || tierIndex >= 6) return 0;
        return data.get(8 + tierIndex);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        int menuSlot = this.slots.indexOf(slot);
        return (menuSlot < 2 || menuSlot > 7) && super.canDragTo(slot);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        int menuSlot = this.slots.indexOf(slot);
        return (menuSlot < 2 || menuSlot > 7)
                && super.canTakeItemForPickAll(stack, slot);
    }

    /** Explicit 64-at-a-time cursor withdrawals and 512-item-safe deposits.
     * Unsupported hotbar swaps, throws, drag merges and cloning are blocked.
     */
    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 2 && slotId <= 7) {
            if (machine == null) return;
            if (type == ClickType.QUICK_MOVE) {
                quickMoveStack(player, slotId);
                broadcastChanges();
                return;
            }
            if (type != ClickType.PICKUP || (button != 0 && button != 1))
                return;
            int backingSlot = slotId - 1;
            ItemStack stored = machine.getInventory().getStackInSlot(backingSlot);
            ItemStack cursor = getCarried();
            if (cursor.isEmpty()) {
                int amount = button == 1 ? 1 : Math.min(64, stored.getCount());
                if (amount > 0)
                    setCarried(machine.getInventory().extractItem(backingSlot, amount, false));
            } else if (stored.isEmpty() || ItemStack.isSameItemSameTags(cursor, stored)) {
                if (InfuserBlockEntity.essenceTier(cursor) != backingSlot - 1) return;
                int amount = Math.min(512 - stored.getCount(),
                        button == 1 ? 1 : cursor.getCount());
                if (amount <= 0) return;
                ItemStack input = cursor.copy();
                input.setCount(amount);
                ItemStack rejected = machine.getInventory().insertItem(backingSlot, input, false);
                int accepted = amount - rejected.getCount();
                if (accepted > 0) {
                    ItemStack remaining = cursor.copy();
                    remaining.shrink(accepted);
                    setCarried(remaining);
                }
            }
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, type, player);
    }

    private SlotItemHandler machineSlot(ItemStackHandler handler, int slot, int x, int y) {
        return new SlotItemHandler(handler, slot, x, y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                if (slot == 8) return false;
                if (slot == 0) return InfuserBlockEntity.isCrystal(stack);
                if (slot >= 1 && slot <= 6) return InfuserBlockEntity.essenceTier(stack) == slot - 1;
                return slot == 7 && net.minecraftforge.common.ForgeHooks.getBurnTime(stack, null) > 0;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }
        };
    }

    public int getEnergy() {
        return (data.get(0) & 0xFFFF) | ((data.get(1) & 0xFFFF) << 16);
    }
    public int getMaximumEnergy() {
        return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16);
    }
    public int getProgress() { return data.get(4); }
    public int getTarget() { return data.get(5); }
    public int getOperationTicks() { return data.get(6); }
    public int getEnergyCostPerTick() { return data.get(7); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.INFUSER.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 1 || buttonId > 5) return false;
        if (machine != null) machine.setSelectedIndex(buttonId);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(slotIndex);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack current = source.getItem();
        ItemStack original = current.copy();
        if (slotIndex >= 2 && slotIndex <= 7) {
            if (machine == null) return ItemStack.EMPTY;
            ItemStack actual = machine.getInventory().getStackInSlot(slotIndex - 1);
            ItemStack batch = actual.copy();
            batch.setCount(Math.min(64, actual.getCount()));
            int before = batch.getCount();
            if (!moveItemStackTo(batch, 10, 46, true)) return ItemStack.EMPTY;
            int moved = before - batch.getCount();
            if (moved <= 0) return ItemStack.EMPTY;
            machine.getInventory().extractItem(slotIndex - 1, moved, false);
            ItemStack result = actual.copy();
            result.setCount(moved);
            return result;
        }
        // 0 upgrade; 1..9 machine; 10..36 player inventory; 37..45 hotbar.
        if (slotIndex < 10) {
            if (!moveItemStackTo(current, 10, 46, true)) return ItemStack.EMPTY;
        } else {
            int target = -1;
            if (current.getItem() instanceof MachineUpgradeItem) target = 0;
            else if (InfuserBlockEntity.isCrystal(current)) target = 1;
            else if (InfuserBlockEntity.essenceTier(current) >= 0)
                target = InfuserBlockEntity.essenceTier(current) + 2;
            else if (net.minecraftforge.common.ForgeHooks.getBurnTime(current, null) > 0) target = 8;
            if (target >= 2 && target <= 7) {
                if (machine == null) return ItemStack.EMPTY;
                ItemStack rejected = machine.getInventory().insertItem(target - 1, current.copy(), false);
                int moved = current.getCount() - rejected.getCount();
                if (moved <= 0) return ItemStack.EMPTY;
                current.shrink(moved);
            } else if (target >= 0) {
                if (!moveItemStackTo(current, target, target + 1, false))
                    return ItemStack.EMPTY;
            } else if (slotIndex < 37) {
                if (!moveItemStackTo(current, 37, 46, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(current, 10, 37, false)) return ItemStack.EMPTY;
            }
        }
        if (current.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        if (current.getCount() == original.getCount()) return ItemStack.EMPTY;
        source.onTake(player, current);
        return original;
    }
}

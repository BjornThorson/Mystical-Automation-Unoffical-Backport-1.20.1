package com.blakebr0.mysticalautomation.container;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.tileentity.CrafterBlockEntity;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Forge 1.20.1 Crafter menu. Recipe slots 12..20 are fake:
 * clicks copy one ghost item from the cursor without consuming it.
 */
public final class CrafterMenu extends AbstractContainerMenu {
    private static final int GHOST_START = 12;
    private static final int GHOST_END = 21;
    private static final int PLAYER_START = 21;
    private static final int TOTAL = 57;

    private final ContainerLevelAccess access;
    private final CrafterBlockEntity machine;
    private final ItemStackHandler pattern;
    private final ContainerData data;

    public CrafterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null,
                new ItemStackHandler(11), new ItemStackHandler(9),
                new UpgradeItemStackHandler(), new SimpleContainerData(8));
    }

    public CrafterMenu(int id, Inventory inventory, CrafterBlockEntity machine) {
        this(id, inventory, machine.getBlockPos(), machine,
                machine.getInventory(), machine.getRecipePattern(),
                machine.getUpgradeInventory(), machine.getDisplayData());
    }

    private CrafterMenu(int id, Inventory playerInventory, BlockPos pos, CrafterBlockEntity machine,
                        ItemStackHandler contents, ItemStackHandler pattern,
                        UpgradeItemStackHandler upgrades, ContainerData data) {
        super(ModMenuTypes.CRAFTER.get(), id);
        this.machine = machine;
        this.pattern = pattern;
        this.data = data;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), pos);

        addSlot(new SlotItemHandler(upgrades, 0, 152, 9) {
            @Override public boolean mayPlace(@NotNull ItemStack item) {
                return item.getItem() instanceof MachineUpgradeItem;
            }
        });
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            addSlot(new SlotItemHandler(contents, i, 8 + i * 18, 101) {
                @Override public boolean mayPlace(@NotNull ItemStack stack) {
                    // Actual resources are pooled, but must be mentioned in the template.
                    for (int j = 0; j < 9; j++)
                        if (!pattern.getStackInSlot(j).isEmpty() &&
                                ItemStack.isSameItemSameTags(pattern.getStackInSlot(j), stack))
                            return true;
                    return false;
                }
            });
        }
        addSlot(new SlotItemHandler(contents, 9, 30, 56) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });
        addSlot(new SlotItemHandler(contents, 10, 148, 48) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++) {
                int slot = col + row * 3;
                addSlot(new SlotItemHandler(pattern, slot, 56 + col * 18, 30 + row * 18) {
                    @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player player) { return false; }
                });
            }
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 135 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 193));
        addDataSlots(data);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= GHOST_START && slotId < GHOST_END) {
            if (clickType == ClickType.PICKUP) {
                ItemStack ghost = getCarried().copy();
                if (!ghost.isEmpty()) ghost.setCount(1);
                int recipeSlot = slotId - GHOST_START;
                if (machine != null) machine.setRecipePatternSlot(recipeSlot, ghost);
                else pattern.setStackInSlot(recipeSlot, ghost);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public int getEnergy() {
        return (data.get(0) & 0xFFFF) | ((data.get(1) & 0xFFFF) << 16);
    }
    public int getMaximumEnergy() {
        return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16);
    }
    public int getProgress() { return data.get(4); }
    public int getOperationTicks() { return data.get(5); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CRAFTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= TOTAL) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 12) {
            if (!moveItemStackTo(stack, PLAYER_START, TOTAL, true)) return ItemStack.EMPTY;
        } else if (index >= PLAYER_START) {
            int dest = -1;
            if (stack.getItem() instanceof MachineUpgradeItem) dest = 0;
            else {
                // A crafting ingredient may also be furnace fuel (wood/planks).
                // Prefer the configured recipe stock over the fuel slot.
                for (int i = 0; i < 9; i++) {
                    if (!pattern.getStackInSlot(i).isEmpty() &&
                            ItemStack.isSameItemSameTags(pattern.getStackInSlot(i), stack)) {
                        dest = i + 1;
                        break;
                    }
                }
                if (dest < 0 && ForgeHooks.getBurnTime(stack, null) > 0) dest = 10;
            }
            if (dest >= 0) {
                if (!moveItemStackTo(stack, dest, dest + 1, false)) return ItemStack.EMPTY;
            } else if (index < 48) {
                if (!moveItemStackTo(stack, 48, TOTAL, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, PLAYER_START, 48, false)) return ItemStack.EMPTY;
            }
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}

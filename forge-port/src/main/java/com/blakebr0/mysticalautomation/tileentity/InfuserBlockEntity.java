package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.blakebr0.mysticalautomation.container.InfuserMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Initial Forge 1.20.1 port of Mystical Automation's Infuser processing engine.
 * Original selection/progression design by BlakeBr0 (MIT).
 * Client rendering verification and 512-item input staging pending.
 */
public class InfuserBlockEntity extends MachineShellBlockEntity implements MenuProvider, IUpgradeableMachine {
    public static final int CRYSTAL = 0;
    public static final int FUEL = 7;
    public static final int OUTPUT = 8;
    public static final int OPERATION_TICKS = 100;
    public static final int FE_PER_TICK = 20;
    public static final int FUEL_MULTIPLIER = 20;

    private static final RegistryObject<Item>[] TIERS = new RegistryObject[] {
            ModItems.INFERIUM_ESSENCE, ModItems.PRUDENTIUM_ESSENCE, ModItems.TERTIUM_ESSENCE,
            ModItems.IMPERIUM_ESSENCE, ModItems.SUPREMIUM_ESSENCE,
            com.blakebr0.mysticalagradditions.init.ModItems.INSANIUM_ESSENCE
    };

    /** Reuses Mystical Agriculture 1.20.1's actual upgrade items and inventory interface. */
    private final UpgradeItemStackHandler upgradeInventory = new UpgradeItemStackHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            InfuserBlockEntity.this.syncUpgradeSettings();
            InfuserBlockEntity.this.setChanged();
        }
    };

    @Override
    public UpgradeItemStackHandler getUpgradeInventory() {
        return upgradeInventory;
    }

    private MachineUpgradeTier upgradeTier() {
        return upgradeInventory.getUpgradeTier();
    }

    public int getOperationTicks() {
        var tier = upgradeTier();
        return tier == null ? OPERATION_TICKS :
                Math.max(1, (int) (OPERATION_TICKS * tier.getOperationTimeMultiplier()));
    }

    public int getEnergyCostPerTick() {
        var tier = upgradeTier();
        return tier == null ? FE_PER_TICK :
                Math.max(1, (int) (FE_PER_TICK * tier.getFuelUsageMultiplier()));
    }

    private void syncUpgradeSettings() {
        var tier = upgradeTier();
        int capacity = tier == null ? BASE_ENERGY_CAPACITY :
                (int) (BASE_ENERGY_CAPACITY * tier.getFuelCapacityMultiplier());
        energy.configure(capacity, getEnergyCostPerTick() * 2);
        if (progress > getOperationTicks()) progress = getOperationTicks();
    }

    private int selectedIndex = 4; // Supremium by default; index 1..5 craftable
    private int progressingIndex = -1;
    private int progress;
    private int fuelLeft;
    private int fuelItemValue;

    private LazyOptional<IItemHandler> upper;
    private LazyOptional<IItemHandler> lower;
    private LazyOptional<IItemHandler> horizontal;

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        makeSidedCaps();
    }

    private void makeSidedCaps() {
        upper = LazyOptional.of(() -> new SidedInventory(Direction.UP));
        lower = LazyOptional.of(() -> new SidedInventory(Direction.DOWN));
        horizontal = LazyOptional.of(() -> new SidedInventory(Direction.NORTH));
    }

    public static boolean isCrystal(ItemStack stack) {
        return !stack.isEmpty() &&
                (stack.is(ModItems.INFUSION_CRYSTAL.get()) || stack.is(ModItems.MASTER_INFUSION_CRYSTAL.get()));
    }

    public static int essenceTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int i = 0; i < TIERS.length; i++) if (stack.is(TIERS[i].get())) return i;
        return -1;
    }

    private int slotForItem(ItemStack stack) {
        if (isCrystal(stack)) return CRYSTAL;
        int tier = essenceTier(stack);
        if (tier >= 0) return tier + 1;
        if (ForgeHooks.getBurnTime(stack, null) > 0) return FUEL;
        return -1;
    }

    /** Returns the count transferred, or 0 if the item does not belong here. */
    public int insertFromPlayer(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        int slot = slotForItem(held);
        if (slot < 0) return 0;
        int old = held.getCount();
        ItemStack remainder = this.inventory.insertItem(slot, held.copy(), false);
        int accepted = old - remainder.getCount();
        if (accepted > 0 && !player.getAbilities().instabuild) {
            held.shrink(accepted);
        }
        return accepted;
    }

    public ItemStack collectOutput() {
        return this.inventory.extractItem(OUTPUT, this.inventory.getStackInSlot(OUTPUT).getCount(), false);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        int safe = Math.max(1, Math.min(5, index));
        if (selectedIndex == safe) return;
        selectedIndex = safe;
        progressingIndex = -1;
        progress = 0;
        setChanged();
    }

    /** Menu property packets transfer signed 16-bit values, so FE is split
     *  into two 16-bit parts to avoid truncating the 80,000 FE capacity. */
    public ContainerData getDisplayData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> energy.getEnergyStored() & 0xFFFF;
                    case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                    case 2 -> energy.getMaxEnergyStored() & 0xFFFF;
                    case 3 -> (energy.getMaxEnergyStored() >>> 16) & 0xFFFF;
                    case 4 -> progress;
                    case 5 -> selectedIndex;
                    case 6 -> getOperationTicks();
                    case 7 -> getEnergyCostPerTick();
                    case 8, 9, 10, 11, 12, 13 ->
                            inventory.getStackInSlot(index - 7).getCount();
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) { /* server authoritative */ }
            @Override
            public int getCount() { return 14; }
        };
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.mysticalautomation.infuser");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new InfuserMenu(id, playerInventory, this);
    }

    public void advanceTarget() {
        this.selectedIndex = selectedIndex == 5 ? 1 : selectedIndex + 1;
        this.progress = 0;
        this.progressingIndex = -1;
        setChanged();
    }

    public String getStatus() {
        return "Target: " + new String[]{"Inferium","Prudentium","Tertium","Imperium","Supremium","Insanium"}[selectedIndex]
                + "; Energy: " + energy.getEnergyStored() + " FE; Progress: " + progress + "/" + OPERATION_TICKS;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, InfuserBlockEntity infuser) {
        infuser.tick();
    }

    private void tick() {
        syncUpgradeSettings();
        // Original machine burns fuel into an internal FE buffer, and can also accept external FE.
        if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            if (fuelLeft <= 0) {
                ItemStack fuel = inventory.getStackInSlot(FUEL);
                int burn = ForgeHooks.getBurnTime(fuel, null);
                if (burn > 0 && canConsumeFuel(fuel)) {
                    ItemStack remaining = fuel.getCraftingRemainingItem();
                    fuelLeft = burn * FUEL_MULTIPLIER;
                    fuelItemValue = fuelLeft;
                    inventory.extractItem(FUEL, 1, false);
                    if (!remaining.isEmpty()) {
                        inventory.setStackInSlot(FUEL, remaining);
                    }
                    setChanged();
                }
            }
            if (fuelLeft > 0) {
                int generated = Math.min(Math.min(fuelLeft, getEnergyCostPerTick() * 2),
                        energy.getMaxEnergyStored() - energy.getEnergyStored());
                int accepted = energy.receiveEnergy(generated, false);
                fuelLeft -= accepted;
                if (fuelLeft == 0) fuelItemValue = 0;
                if (accepted > 0) setChanged();
            }
        }
        ItemStack crystal = inventory.getStackInSlot(CRYSTAL);
        if (!isCrystal(crystal)) {
            progress = 0;
            progressingIndex = -1;
            return;
        }
        int index = chooseProcessingIndex();
        if (index < 0) {
            progress = 0;
            progressingIndex = -1;
            return;
        }
        if (index != progressingIndex) {
            progress = 0;
            progressingIndex = index;
        }
        int destSlot = (index + 1 == selectedIndex) ? OUTPUT : index + 2;
        ItemStack output = inventory.getStackInSlot(destSlot);
        ItemStack product = new ItemStack(TIERS[index + 1].get());
        if (!output.isEmpty() && (!ItemStack.isSameItemSameTags(output, product) ||
                output.getCount() >= Math.min(inventory.getSlotLimit(destSlot), output.getMaxStackSize()))) {
            return;
        }
        int needed = getEnergyCostPerTick();
        if (energy.getEnergyStored() < needed) return;
        consumeEnergy(needed);
        ++progress;
        setChanged();
        if (progress < getOperationTicks()) return;

        // Check all preconditions once more before mutating stacks.
        if (inventory.getStackInSlot(index + 1).getCount() < 4
                || !inventory.getStackInSlot(index + 1).is(TIERS[index].get())
                || !isCrystal(inventory.getStackInSlot(CRYSTAL))) {
            progress = 0;
            return;
        }
        inventory.extractItem(index + 1, 4, false);
        ItemStack grown = product.copy();
        grown.setCount(output.getCount() + 1);
        inventory.setStackInSlot(destSlot, grown);
        ItemStack remainingCrystal = crystal.getCraftingRemainingItem();
        inventory.setStackInSlot(CRYSTAL, remainingCrystal);
        progress = 0;
        progressingIndex = -1;
        setChanged();
    }

    private boolean canConsumeFuel(ItemStack fuel) {
        if (fuel.isEmpty()) return false;
        ItemStack remaining = fuel.getCraftingRemainingItem();
        return remaining.isEmpty() || fuel.getCount() == 1;
    }

    private int chooseProcessingIndex() {
        if (progress > 0 && progressingIndex >= 0 && progressingIndex < selectedIndex) {
            ItemStack current = inventory.getStackInSlot(progressingIndex + 1);
            if (current.getCount() >= 4 && current.is(TIERS[progressingIndex].get()))
                return progressingIndex;
        }
        for (int i = selectedIndex - 1; i >= 0; i--) {
            ItemStack stack = inventory.getStackInSlot(i + 1);
            if (stack.getCount() >= 4 && stack.is(TIERS[i].get())) return i;
        }
        return -1;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP) return upper.cast();
            if (side == Direction.DOWN) return lower.cast();
            return horizontal.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        upper.invalidate();
        lower.invalidate();
        horizontal.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        makeSidedCaps();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("UpgradeInventory", upgradeInventory.serializeNBT());
        tag.putInt("SelectedIndex", selectedIndex);
        tag.putInt("ProgressingIndex", progressingIndex);
        tag.putInt("Progress", progress);
        tag.putInt("FuelLeft", fuelLeft);
        tag.putInt("FuelItemValue", fuelItemValue);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("UpgradeInventory", 10))
            upgradeInventory.deserializeNBT(tag.getCompound("UpgradeInventory"));
        syncUpgradeSettings();
        selectedIndex = Math.max(1, Math.min(5, tag.contains("SelectedIndex") ? tag.getInt("SelectedIndex") : 4));
        progressingIndex = Math.max(-1, Math.min(4, tag.getInt("ProgressingIndex")));
        progress = Math.max(0, Math.min(getOperationTicks(), tag.getInt("Progress")));
        fuelLeft = Math.max(0, tag.getInt("FuelLeft"));
        fuelItemValue = Math.max(0, tag.getInt("FuelItemValue"));
    }

    private final class SidedInventory implements IItemHandler {
        private final Direction side;
        SidedInventory(Direction side) { this.side = side; }

        @Override
        public int getSlots() { return inventory.getSlots(); }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (side == Direction.DOWN && slot == OUTPUT) return inventory.extractItem(slot, amount, simulate);
            if (side == Direction.NORTH && slot == FUEL && ForgeHooks.getBurnTime(inventory.getStackInSlot(FUEL), null) <= 0) {
                return inventory.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (side == Direction.UP) {
                if (slot == CRYSTAL) return isCrystal(stack);
                return slot >= 1 && slot <= 6 && essenceTier(stack) == slot - 1;
            }
            return side == Direction.NORTH && slot == FUEL && ForgeHooks.getBurnTime(stack, null) > 0;
        }
    }
}


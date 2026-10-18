package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.mysticalautomation.crafting.FarmerRecipe;
import com.blakebr0.mysticalautomation.init.ModFarmerRecipes;
import com.blakebr0.mysticalautomation.block.MachineShellBlock;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import com.blakebr0.mysticalautomation.container.FarmerMenu;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Recipe-based virtual Farmer following upstream Mystical Automation's 800-tick
 * base operation, staged progress, permanent seed/soil/crux inputs and rolled
 * harvest outputs. This is a server processor, GUI still pending.
 */
public final class FarmerBlockEntity extends MachineShellBlockEntity implements IUpgradeableMachine, MenuProvider {
    public static final int FUEL = 3;
    public static final int FIRST_OUTPUT = 4;
    public static final int LAST_OUTPUT = 12;
    public static final int BASE_OPERATION_TICKS = 800;
    public static final int BASE_COST = 20;
    private final UpgradeItemStackHandler upgrades = new UpgradeItemStackHandler() {
        @Override protected void onContentsChanged(int slot) {
            FarmerBlockEntity.this.updateUpgrade();
            FarmerBlockEntity.this.setChanged();
        }
    };
    private LazyOptional<IItemHandler> top, bottom, side;
    private int progress, stages, stageProgress, fuelLeft;
    private boolean running;

    public FarmerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        initCaps();
    }
    @Override public UpgradeItemStackHandler getUpgradeInventory() { return upgrades; }

    private void updateUpgrade() {
        MachineUpgradeTier tier = upgrades.getUpgradeTier();
        energy.configure(tier == null ? BASE_ENERGY_CAPACITY :
                (int)(BASE_ENERGY_CAPACITY * tier.getFuelCapacityMultiplier()),
                getEnergyCostPerTick() * 2);
    }
    public int getOperationTicks() {
        var tier = upgrades.getUpgradeTier();
        return tier == null ? BASE_OPERATION_TICKS :
                Math.max(1, (int)(BASE_OPERATION_TICKS * tier.getOperationTimeMultiplier()));
    }
    public int getEnergyCostPerTick() {
        var tier = upgrades.getUpgradeTier();
        return tier == null ? BASE_COST :
                Math.max(1, (int)(BASE_COST * tier.getFuelUsageMultiplier()));
    }
    public int getGrowthStage() { return progress; }
    public int getStageProgress() { return stageProgress; }
    public int getStages() { return stages; }
    public boolean isRunning() { return running; }

    /** 32-bit FE fields are split for Minecraft's short-sized ContainerData packets. */
    public ContainerData getDisplayData() {
        return new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> energy.getEnergyStored() & 0xFFFF;
                    case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                    case 2 -> energy.getMaxEnergyStored() & 0xFFFF;
                    case 3 -> (energy.getMaxEnergyStored() >>> 16) & 0xFFFF;
                    case 4 -> fuelLeft;
                    case 5 -> ForgeHooks.getBurnTime(inventory.getStackInSlot(FUEL), null);
                    case 6 -> progress;
                    case 7 -> getOperationTicks();
                    case 8 -> stages;
                    case 9 -> stageProgress;
                    case 10 -> getOperationTicks() * stages;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { /* server authoritative */ }
            @Override public int getCount() { return 11; }
        };
    }
    @Override public Component getDisplayName() {
        return Component.translatable("container.mysticalautomation.farmer");
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FarmerMenu(id, inventory, this);
    }


    public static void tickServer(Level level, BlockPos pos, BlockState state, FarmerBlockEntity farmer) {
        farmer.tick();
    }

    private void tick() {
        updateUpgrade();
        if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            if (fuelLeft <= 0) {
                ItemStack fuel = inventory.getStackInSlot(FUEL);
                int burn = ForgeHooks.getBurnTime(fuel, null);
                if (burn > 0 && fuel.getCraftingRemainingItem().isEmpty()) {
                    inventory.extractItem(FUEL, 1, false);
                    fuelLeft = burn * 20;
                    setChanged();
                }
            }
            if (fuelLeft > 0) {
                int transferred = energy.receiveEnergy(Math.min(fuelLeft, getEnergyCostPerTick() * 2), false);
                fuelLeft -= transferred;
                if (transferred > 0) setChanged();
            }
        }

        var recipe = getActiveRecipe();
        boolean powered = energy.getEnergyStored() >= getEnergyCostPerTick();
        boolean active = recipe != null && powered;
        if (active) {
            stages = recipe.getStages();
            // Never discard rolled output if all nine output slots are full.
            if (progress >= getOperationTicks() - 1 && stageProgress >= getOperationTicks() * stages) {
                List<ItemStack> results = recipe.rollResults(level.random);
                if (canFitResults(results)) {
                    consumeEnergy(getEnergyCostPerTick());
                    addResults(results);
                    resetGrowth();
                }
            } else {
                consumeEnergy(getEnergyCostPerTick());
                if (stageProgress >= getOperationTicks() * stages) progress++;
                else stageProgress++;
                setChanged();
            }
        } else if (recipe == null && (progress != 0 || stageProgress != 0 || stages != 0)) {
            resetGrowth();
        }
        if (running != active) {
            running = active;
            if (level.getBlockState(worldPosition).hasProperty(MachineShellBlock.RUNNING)) {
                level.setBlock(worldPosition,
                        level.getBlockState(worldPosition).setValue(MachineShellBlock.RUNNING, active), 3);
            }
            setChanged();
        }
    }

    private FarmerRecipe getActiveRecipe() {
        if (level == null) return null;
        var inputs = new SimpleContainer(inventory.getStackInSlot(0).copy(),
                inventory.getStackInSlot(1).copy(), inventory.getStackInSlot(2).copy());
        return level.getRecipeManager().getRecipeFor(ModFarmerRecipes.TYPE.get(), inputs, level).orElse(null);
    }

    private boolean canFitResults(List<ItemStack> results) {
        var test = new ItemStackHandler(LAST_OUTPUT - FIRST_OUTPUT + 1);
        for (int slot = FIRST_OUTPUT; slot <= LAST_OUTPUT; slot++)
            test.setStackInSlot(slot - FIRST_OUTPUT, inventory.getStackInSlot(slot).copy());
        for (ItemStack result : results) {
            ItemStack leftover = result.copy();
            for (int slot = 0; slot < test.getSlots() && !leftover.isEmpty(); slot++)
                leftover = test.insertItem(slot, leftover, false);
            if (!leftover.isEmpty()) return false;
        }
        return true;
    }

    private void addResults(List<ItemStack> results) {
        for (ItemStack result : results) {
            ItemStack leftover = result.copy();
            for (int slot = FIRST_OUTPUT; slot <= LAST_OUTPUT && !leftover.isEmpty(); slot++)
                leftover = inventory.insertItem(slot, leftover, false);
            if (!leftover.isEmpty()) throw new IllegalStateException("Farmer inventory plan changed during harvest");
        }
    }

    private void resetGrowth() {
        progress = 0;
        stages = 0;
        stageProgress = 0;
        setChanged();
    }

    private void initCaps() {
        top = LazyOptional.of(() -> new Side(Direction.UP));
        bottom = LazyOptional.of(() -> new Side(Direction.DOWN));
        side = LazyOptional.of(() -> new Side(Direction.NORTH));
    }
    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap,
                                                                   @Nullable Direction direction) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return (direction == Direction.UP ? top : direction == Direction.DOWN ? bottom : side).cast();
        return super.getCapability(cap, direction);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps(); top.invalidate(); bottom.invalidate(); side.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps(); initCaps();
    }
    private final class Side implements IItemHandler {
        private final Direction direction;
        Side(Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return inventory.getSlots(); }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int count, boolean simulate) {
            if (direction == Direction.DOWN && slot >= FIRST_OUTPUT && slot <= LAST_OUTPUT)
                return inventory.extractItem(slot, count, simulate);
            if (direction == Direction.NORTH && slot == FUEL &&
                    ForgeHooks.getBurnTime(inventory.getStackInSlot(FUEL), null) <= 0)
                return inventory.extractItem(FUEL, count, simulate);
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (direction == Direction.UP && slot >= 0 && slot <= 2)
                return stack.getCount() > 0;
            return direction == Direction.NORTH && slot == FUEL &&
                    ForgeHooks.getBurnTime(stack, null) > 0;
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("UpgradeInventory", upgrades.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("Stages", stages);
        tag.putInt("StageProgress", stageProgress);
        tag.putInt("FuelLeft", fuelLeft);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("UpgradeInventory", 10))
            upgrades.deserializeNBT(tag.getCompound("UpgradeInventory"));
        updateUpgrade();
        progress = Math.max(0, tag.getInt("Progress"));
        stages = Math.max(0, tag.getInt("Stages"));
        stageProgress = Math.max(0, tag.getInt("StageProgress"));
        fuelLeft = Math.max(0, tag.getInt("FuelLeft"));
    }
}

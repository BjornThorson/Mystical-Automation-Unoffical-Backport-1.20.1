package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.mysticalautomation.block.MachineShellBlock;
import com.blakebr0.mysticalautomation.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Temporary shared 1.20.1 machine tile. Preserves each machine's inventory size
 * and energy capacity while the seven original processors are backported.
 * Upgrade slots, sided inventory routing and processing logic are still TODO.
 */
public class MachineShellBlockEntity extends BlockEntity {
    public static final int BASE_ENERGY_CAPACITY = 80_000;
    protected final ItemStackHandler inventory;
    protected final ManagedEnergyStorage energy;
    private LazyOptional<IItemHandler> itemCapability;
    private LazyOptional<IEnergyStorage> energyCapability;

    public MachineShellBlockEntity(BlockPos pos, BlockState state) {
        super(((MachineShellBlock)state.getBlock()).getMachineType(), pos, state);
        int slots = ((MachineShellBlock)state.getBlock()).getSlotCount();
        // Upstream Infuser provides six 512-item ESSENCE staging slots.
        // Override Forge's getStackLimit too: its vanilla default caps each
        // slot at ItemStack.getMaxStackSize (64), even if getSlotLimit is 512.
        final boolean infuser = state.getBlock() == ModBlocks.INFUSER.get();
        final boolean farmer = state.getBlock() == ModBlocks.FARMER.get();
        this.inventory = new ItemStackHandler(slots) {
            @Override
            public int getSlotLimit(int slot) {
                if (infuser && slot >= 1 && slot <= 6) return 512;
                if (farmer && slot >= 0 && slot <= 2) return 1;
                return super.getSlotLimit(slot);
            }

            @Override
            protected int getStackLimit(int slot, ItemStack stack) {
                if (infuser && slot >= 1 && slot <= 6) return 512;
                if (farmer && slot >= 0 && slot <= 2) return 1;
                return super.getStackLimit(slot, stack);
            }

            /**
             * ItemStack.save in Minecraft 1.20.1 stores Count in a signed byte.
             * Preserve large Infuser stacks with a separate INT and a safe
             * vanilla Count marker. This is an NBT extension, not a vanilla
             * ItemStack format change.
             */
            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = super.serializeNBT();
                if (infuser) {
                    var items = tag.getList("Items", 10);
                    for (int index = 0; index < items.size(); index++) {
                        CompoundTag itemTag = items.getCompound(index);
                        int slot = itemTag.getInt("Slot");
                        if (slot >= 1 && slot <= 6) {
                            ItemStack actual = getStackInSlot(slot);
                            if (actual.getCount() > 64) {
                                itemTag.putByte("Count", (byte) 1);
                                itemTag.putInt("LargeStackCount", actual.getCount());
                            }
                        }
                    }
                }
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                super.deserializeNBT(tag);
                if (infuser) {
                    var items = tag.getList("Items", 10);
                    for (int index = 0; index < items.size(); index++) {
                        CompoundTag itemTag = items.getCompound(index);
                        int slot = itemTag.getInt("Slot");
                        if (slot >= 1 && slot <= 6 && itemTag.contains("LargeStackCount", 3)) {
                            int count = itemTag.getInt("LargeStackCount");
                            if (count >= 1 && count <= 512) {
                                ItemStack original = getStackInSlot(slot);
                                if (!original.isEmpty()) {
                                    ItemStack restored = original.copy();
                                    restored.setCount(count);
                                    setStackInSlot(slot, restored);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            protected void onContentsChanged(int slot) {
                MachineShellBlockEntity.this.setChanged();
            }
        };
        this.energy = new ManagedEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int accepted = super.receiveEnergy(maxReceive, simulate);
                if (!simulate && accepted > 0) {
                    MachineShellBlockEntity.this.setChanged();
                }
                return accepted;
            }
        };
        this.createCaps();
    }

    /** Forge capability exposes FE reception only. Internal machines consume energy explicitly. */
    protected class ManagedEnergyStorage extends EnergyStorage {
        public ManagedEnergyStorage() {
            super(BASE_ENERGY_CAPACITY, 1000, 0);
        }
        /** Reconfigure available FE storage for the original Mystical Agriculture upgrade tiers. */
        public void configure(int desiredCapacity, int desiredReceive) {
            int newCapacity = Math.max(BASE_ENERGY_CAPACITY, desiredCapacity);
            int newReceive = Math.max(1000, desiredReceive);
            if (this.capacity != newCapacity || this.maxReceive != newReceive) {
                this.capacity = newCapacity;
                this.maxReceive = newReceive;
                if (this.energy > newCapacity) this.energy = newCapacity;
                MachineShellBlockEntity.this.setChanged();
            }
        }

        public int consumeInternal(int requested) {
            int taken = Math.min(Math.max(requested, 0), this.energy);
            if (taken > 0) {
                this.energy -= taken;
                MachineShellBlockEntity.this.setChanged();
            }
            return taken;
        }
    }

    protected int consumeEnergy(int requested) {
        return this.energy.consumeInternal(requested);
    }

    private void createCaps() {
        this.itemCapability = LazyOptional.of(() -> this.inventory);
        this.energyCapability = LazyOptional.of(() -> this.energy);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public IEnergyStorage getEnergy() {
        return this.energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.put("Energy", this.energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory", 10)) {
            this.inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("Energy", 3)) {
            this.energy.deserializeNBT(tag.get("Energy"));
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemCapability.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return this.energyCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemCapability.invalidate();
        this.energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.createCaps();
    }
}

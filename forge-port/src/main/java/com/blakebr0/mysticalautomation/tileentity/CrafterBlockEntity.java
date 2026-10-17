package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import com.blakebr0.mysticalautomation.container.CrafterMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
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
import java.util.Arrays;
import java.util.List;

/**
 * First functional Crafter processor, derived from upstream Mystical Automation
 * 1.21.1 implementation (MIT). 9-slot ghost recipe template, 9 real input slots,
 * fuel slot 9 and output slot 10. GUI and pattern entry remain to be ported.
 */
public final class CrafterBlockEntity extends MachineShellBlockEntity implements IUpgradeableMachine, MenuProvider {
    public static final int FUEL = 9;
    public static final int OUTPUT = 10;
    public static final int BASE_OPERATION_TICKS = 200;
    public static final int BASE_FE_PER_TICK = 20;
    private final ItemStackHandler pattern = new ItemStackHandler(9) {
        @Override
        public int getSlotLimit(int slot) { return 1; }
        @Override
        protected void onContentsChanged(int slot) {
            CrafterBlockEntity.this.progress = 0;
            CrafterBlockEntity.this.setChanged();
        }
    };
    private final UpgradeItemStackHandler upgrades = new UpgradeItemStackHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            CrafterBlockEntity.this.updateUpgrade();
            CrafterBlockEntity.this.setChanged();
        }
    };
    private int progress;
    private int fuelLeft;
    private LazyOptional<IItemHandler> top;
    private LazyOptional<IItemHandler> bottom;
    private LazyOptional<IItemHandler> side;

    public CrafterBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        initCaps();
    }

    @Override
    public UpgradeItemStackHandler getUpgradeInventory() { return upgrades; }
    public ItemStackHandler getRecipePattern() { return pattern; }
    public ContainerData getDisplayData() {
        return new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> energy.getEnergyStored() & 0xFFFF;
                    case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                    case 2 -> energy.getMaxEnergyStored() & 0xFFFF;
                    case 3 -> (energy.getMaxEnergyStored() >>> 16) & 0xFFFF;
                    case 4 -> progress;
                    case 5 -> getOperationTicks();
                    case 6 -> fuelLeft;
                    case 7 -> getEnergyCostPerTick();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { /* server owns values */ }
            @Override public int getCount() { return 8; }
        };
    }

    @Override public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.mysticalautomation.crafter");
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CrafterMenu(id, inventory, this);
    }


    public void setRecipePatternSlot(int slot, ItemStack template) {
        if (slot < 0 || slot >= 9) throw new IllegalArgumentException("pattern slot");
        ItemStack ghost = template.copy();
        if (!ghost.isEmpty()) ghost.setCount(1);
        pattern.setStackInSlot(slot, ghost);
    }

    private void updateUpgrade() {
        MachineUpgradeTier tier = upgrades.getUpgradeTier();
        energy.configure(tier == null ? BASE_ENERGY_CAPACITY :
                (int)(BASE_ENERGY_CAPACITY * tier.getFuelCapacityMultiplier()), getEnergyCostPerTick() * 2);
        if (progress > getOperationTicks()) progress = getOperationTicks();
    }

    public int getOperationTicks() {
        var tier = upgrades.getUpgradeTier();
        return tier == null ? BASE_OPERATION_TICKS :
                Math.max(1, (int)(BASE_OPERATION_TICKS * tier.getOperationTimeMultiplier()));
    }
    public int getEnergyCostPerTick() {
        var tier = upgrades.getUpgradeTier();
        return tier == null ? BASE_FE_PER_TICK :
                Math.max(1, (int)(BASE_FE_PER_TICK * tier.getFuelUsageMultiplier()));
    }

    public String debugStatus() {
        var crafting = craftingGrid();
        var recipe = level == null ? java.util.Optional.<CraftingRecipe>empty() :
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level);
        var output = inventory.getStackInSlot(OUTPUT);
        return "recipe=" + recipe.map(x -> x.getId().toString()).orElse("none") +
                ",template0=" + pattern.getStackInSlot(0) +
                ",stock0=" + inventory.getStackInSlot(0) +
                ",power=" + energy.getEnergyStored() +
                ",progress=" + progress + ",result=" + output;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, CrafterBlockEntity crafter) {
        crafter.tick();
    }

    private void tick() {
        updateUpgrade();
        if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            if (fuelLeft <= 0) {
                ItemStack stack = inventory.getStackInSlot(FUEL);
                int burn = ForgeHooks.getBurnTime(stack, null);
                // No remainder overflow: consume only ordinary non-container fuel.
                if (burn > 0 && stack.getCraftingRemainingItem().isEmpty()) {
                    inventory.extractItem(FUEL, 1, false);
                    fuelLeft = burn * 20;
                }
            }
            if (fuelLeft > 0) {
                int accepted = energy.receiveEnergy(Math.min(fuelLeft, getEnergyCostPerTick() * 2), false);
                fuelLeft -= accepted;
                if (accepted > 0) setChanged();
            }
        }
        if (level == null) return;
        CraftingContainer grid = craftingGrid();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElse(null);
        if (recipe == null) { resetProgress(); return; }
        var result = recipe.assemble(grid, level.registryAccess());
        if (result.isEmpty()) { resetProgress(); return; }
        int[] used = new int[9];
        ItemStack[] remainders = new ItemStack[9];
        Arrays.fill(remainders, ItemStack.EMPTY);
        if (!makeConsumptionPlan(recipe, grid, used, remainders) || !canOutput(result)) {
            resetProgress();
            return;
        }
        int cost = getEnergyCostPerTick();
        if (energy.getEnergyStored() < cost) return;
        consumeEnergy(cost);
        progress++;
        setChanged();
        if (progress < getOperationTicks()) return;

        // Recheck consumables before modifying the inventory.
        for (int i = 0; i < 9; i++) {
            if (inventory.getStackInSlot(i).getCount() < used[i]) {
                resetProgress();
                return;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (used[i] == 0) continue;
            ItemStack old = inventory.getStackInSlot(i);
            ItemStack next = old.copy();
            next.shrink(used[i]);
            if (!remainders[i].isEmpty()) {
                // Byproduct safely fits only when source stack was exhausted.
                next = remainders[i];
            }
            inventory.setStackInSlot(i, next);
        }
        ItemStack output = inventory.getStackInSlot(OUTPUT);
        ItemStack newOutput = result.copy();
        newOutput.setCount(result.getCount() + output.getCount());
        inventory.setStackInSlot(OUTPUT, newOutput);
        progress = 0;
        setChanged();
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    private boolean canOutput(ItemStack result) {
        ItemStack existing = inventory.getStackInSlot(OUTPUT);
        return existing.isEmpty() && result.getCount() <= result.getMaxStackSize()
                || !existing.isEmpty() && ItemStack.isSameItemSameTags(existing, result)
                && existing.getCount() + result.getCount() <=
                Math.min(existing.getMaxStackSize(), inventory.getSlotLimit(OUTPUT));
    }

    /** Matches the pattern's ingredient alternatives against pooled input items,
     * reserving each actual ingredient before another recipe slot can use it.
     * Crafting remainders can return to an exhausted source slot. If a container
     * remainder has nowhere safe to go, crafting is blocked instead of deleting it.
     */
    private boolean makeConsumptionPlan(CraftingRecipe recipe, CraftingContainer grid,
                                        int[] used, ItemStack[] byproducts) {
        int[] remaining = new int[9];
        for (int i = 0; i < 9; i++) remaining[i] = inventory.getStackInSlot(i).getCount();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<ItemStack> residue = recipe.getRemainingItems(grid);
        // The ingredient list can be a compact 2x2 shape, while recipe
        // remainders are indexed by the whole 3x3 crafting grid. Match each
        // ingredient to a unique template cell before looking up its remainder.
        boolean[] patternCellAssigned = new boolean[9];
        for (int k = 0; k < ingredients.size(); k++) {
            Ingredient ingredient = ingredients.get(k);
            if (ingredient.isEmpty()) continue;
            int patternCell = -1;
            for (int cell = 0; cell < 9; cell++) {
                if (!patternCellAssigned[cell] && ingredient.test(grid.getItem(cell))) {
                    patternCell = cell;
                    break;
                }
            }
            if (patternCell < 0) return false;
            patternCellAssigned[patternCell] = true;

            int supplySlot = -1;
            for (int i = 0; i < 9; i++) {
                if (remaining[i] > 0 && ingredient.test(inventory.getStackInSlot(i))) {
                    supplySlot = i;
                    break;
                }
            }
            if (supplySlot < 0) return false;
            remaining[supplySlot]--;
            used[supplySlot]++;

            ItemStack leftover = residue.get(patternCell);
            if (!leftover.isEmpty()) {
                ItemStack previous = byproducts[supplySlot];
                if (previous.isEmpty()) {
                    byproducts[supplySlot] = leftover.copy();
                } else if (ItemStack.isSameItemSameTags(previous, leftover)
                        && previous.getCount() + leftover.getCount() <= previous.getMaxStackSize()) {
                    ItemStack combined = previous.copy();
                    combined.grow(leftover.getCount());
                    byproducts[supplySlot] = combined;
                } else return false;
            }
        }
        for (int i = 0; i < 9; i++) {
            // Avoid deleting craft remainders when input stack has unconsumed items.
            if (!byproducts[i].isEmpty() && remaining[i] != 0) return false;
        }
        return true;
    }

    private CraftingContainer craftingGrid() {
        // Template-only inventory; the nine real ingredient stacks are separate.
        AbstractContainerMenu dummy = new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public ItemStack quickMoveStack(Player p, int index) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player p) { return false; }
        };
        TransientCraftingContainer grid = new TransientCraftingContainer(dummy, 3, 3);
        for (int i = 0; i < 9; i++)
            grid.setItem(i, pattern.getStackInSlot(i).copy());
        return grid;
    }

    private void initCaps() {
        top = LazyOptional.of(() -> new InputHandler(Direction.UP));
        bottom = LazyOptional.of(() -> new InputHandler(Direction.DOWN));
        side = LazyOptional.of(() -> new InputHandler(Direction.NORTH));
    }
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return (direction == Direction.UP ? top :
                    direction == Direction.DOWN ? bottom : side).cast();
        }
        return super.getCapability(cap, direction);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps(); top.invalidate(); bottom.invalidate(); side.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps(); initCaps();
    }

    private final class InputHandler implements IItemHandler {
        private final Direction direction;
        InputHandler(Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return inventory.getSlots(); }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (direction == Direction.DOWN && slot == OUTPUT)
                return inventory.extractItem(slot, amount, simulate);
            if (direction == Direction.NORTH && slot == FUEL &&
                    ForgeHooks.getBurnTime(inventory.getStackInSlot(FUEL), null) == 0)
                return inventory.extractItem(slot, amount, simulate);
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (direction == Direction.UP && slot >= 0 && slot < 9) {
                return !pattern.getStackInSlot(slot).isEmpty() &&
                        ItemStack.isSameItemSameTags(stack, pattern.getStackInSlot(slot));
            }
            return direction == Direction.NORTH && slot == FUEL && ForgeHooks.getBurnTime(stack, null) > 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("RecipeInventory", pattern.serializeNBT());
        tag.put("UpgradeInventory", upgrades.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("FuelLeft", fuelLeft);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RecipeInventory", 10))
            pattern.deserializeNBT(tag.getCompound("RecipeInventory"));
        if (tag.contains("UpgradeInventory", 10))
            upgrades.deserializeNBT(tag.getCompound("UpgradeInventory"));
        progress = Math.max(0, Math.min(getOperationTicks(), tag.getInt("Progress")));
        fuelLeft = Math.max(0, tag.getInt("FuelLeft"));
        updateUpgrade();
    }
}

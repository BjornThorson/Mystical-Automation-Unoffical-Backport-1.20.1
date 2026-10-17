package com.blakebr0.mysticalautomation.gametest;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.container.InfuserMenu;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.tileentity.InfuserBlockEntity;
import com.blakebr0.mysticalagriculture.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;

/** Runs only when Minecraft's game-test server is explicitly launched. */
@GameTestHolder("mysticalautomation")
public final class InfuserGameTests {
    @GameTest(template = "empty", timeoutTicks = 180)
    public static void imperiumToSupremium(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        tile.getInventory().setStackInSlot(0, new ItemStack(ModItems.MASTER_INFUSION_CRYSTAL.get()));
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        for (int i = 0; i < 4; i++) tile.getEnergy().receiveEnergy(1000, false);

        helper.runAfterDelay(115, () -> {
            ItemStack output = tile.getInventory().getStackInSlot(8);
            if (output.is(ModItems.SUPREMIUM_ESSENCE.get()) && output.getCount() == 1
                    && tile.getInventory().getStackInSlot(4).isEmpty()
                    && tile.getInventory().getStackInSlot(0).is(ModItems.MASTER_INFUSION_CRYSTAL.get())) {
                helper.succeed();
            } else {
                helper.fail("Infuser failed to craft one Supremium from four Imperium while preserving its Master Crystal");
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void noEnergyNoCraft(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        tile.getInventory().setStackInSlot(0, new ItemStack(ModItems.MASTER_INFUSION_CRYSTAL.get()));
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        helper.runAfterDelay(105, () -> {
            if (tile.getInventory().getStackInSlot(8).isEmpty() &&
                tile.getInventory().getStackInSlot(4).getCount() == 4) helper.succeed();
            else helper.fail("Infuser converted essence without FE or fuel");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void fullOutputBlocksProduction(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        tile.getInventory().setStackInSlot(0, new ItemStack(ModItems.MASTER_INFUSION_CRYSTAL.get()));
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        tile.getInventory().setStackInSlot(8, new ItemStack(ModItems.SUPREMIUM_ESSENCE.get(), 64));
        for (int i = 0; i < 4; i++) tile.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(115, () -> {
            if (tile.getInventory().getStackInSlot(8).getCount() == 64
                    && tile.getInventory().getStackInSlot(4).getCount() == 4) helper.succeed();
            else helper.fail("Blocked Infuser output consumed inputs or overflowed the output stack");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void coalFuelPowersConversion(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        tile.getInventory().setStackInSlot(0, new ItemStack(ModItems.MASTER_INFUSION_CRYSTAL.get()));
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        tile.getInventory().setStackInSlot(7, new ItemStack(Items.COAL));
        helper.runAfterDelay(118, () -> {
            if (tile.getInventory().getStackInSlot(8).is(ModItems.SUPREMIUM_ESSENCE.get())
                    && tile.getInventory().getStackInSlot(7).isEmpty()) helper.succeed();
            else helper.fail("Fuelled Infuser did not complete a conversion");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void basicCrystalConsumesDurability(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        ItemStack crystal = new ItemStack(ModItems.INFUSION_CRYSTAL.get());
        int priorDamage = crystal.getDamageValue();
        tile.getInventory().setStackInSlot(0, crystal);
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        for (int i = 0; i < 4; i++) tile.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(115, () -> {
            ItemStack after = tile.getInventory().getStackInSlot(0);
            if (tile.getInventory().getStackInSlot(8).is(ModItems.SUPREMIUM_ESSENCE.get())
                    && after.is(ModItems.INFUSION_CRYSTAL.get()) && after.getDamageValue() > priorDamage)
                helper.succeed();
            else helper.fail("Standard Infusion Crystal did not consume durability after crafting");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void inventoryAndEnergySaveLoad(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.INFUSER.get());
        InfuserBlockEntity tile = (InfuserBlockEntity) helper.getBlockEntity(relative);
        tile.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 7));
        tile.getEnergy().receiveEnergy(600, false);
        tile.advanceTarget();
        CompoundTag saved = tile.saveWithFullMetadata();
        InfuserBlockEntity restored = new InfuserBlockEntity(tile.getBlockPos(), tile.getBlockState());
        restored.load(saved);
        if (restored.getInventory().getStackInSlot(4).getCount() == 7
                && restored.getEnergy().getEnergyStored() == 600
                && restored.getSelectedIndex() == 5) helper.succeed();
        else helper.fail("Infuser NBT round-trip did not preserve items, energy or selected tier");
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void infuserRecipeRegistered(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(
                new ResourceLocation("mysticalautomation", "infuser"));
        if (recipe.isPresent()) helper.succeed();
        else helper.fail("Infuser crafting recipe missing from the Forge recipe registry");
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void menuSelectionSyncData(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        InfuserMenu menu = new InfuserMenu(1, player.getInventory(), machine);
        if (menu.getType() != ModMenuTypes.INFUSER.get() || menu.slots.size() != 46) {
            helper.fail("Infuser Forge menu registration or 1+9+36 slot layout incorrect");
            return;
        }
        for (int i = 0; i < 80; i++) machine.getEnergy().receiveEnergy(1_000, false);
        if (menu.getMaximumEnergy() != 80_000 || menu.getEnergy() != 80_000) {
            helper.fail("Infuser FE menu sync truncated 80k buffer");
            return;
        }
        if (menu.getTarget() != 4) {
            helper.fail("Infuser menu did not expose the initial Supremium target");
            return;
        }
        // Mirror the graphical right/left arrows: Supremium -> Insanium -> Supremium.
        if (!menu.clickMenuButton(player, 5)
                || machine.getSelectedIndex() != 5 || menu.getTarget() != 5) {
            helper.fail("Infuser menu did not synchronize right-arrow selection of Insanium");
            return;
        }
        if (!menu.clickMenuButton(player, 4)
                || machine.getSelectedIndex() != 4 || menu.getTarget() != 4) {
            helper.fail("Infuser menu did not synchronize left-arrow selection of Supremium");
            return;
        }
        if (!menu.clickMenuButton(player, 3)
                || machine.getSelectedIndex() != 3 || menu.getTarget() != 3) {
            helper.fail("Infuser menu tier button did not select and synchronize Imperium");
            return;
        }
        if (menu.clickMenuButton(player, 99)
                || machine.getSelectedIndex() != 3 || menu.getTarget() != 3) {
            helper.fail("Infuser menu accepted an invalid tier selector or changed state");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void upgradeScalesSpeedPowerAndCapacity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.IMPERIUM_UPGRADE.get()));
        if (machine.getOperationTicks() != 12 ||
                machine.getEnergyCostPerTick() != 320 ||
                machine.getEnergy().getMaxEnergyStored() != 400_000) {
            helper.fail("Imperium upgrade did not apply the original 0.125x speed, 16x FE use and 5x FE capacity");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void upgradeReplacementAndNbtPersistence(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.INFERIUM_UPGRADE.get()));
        var older = machine.applyUpgrade((MachineUpgradeItem) ModItems.SUPREMIUM_UPGRADE.get());
        if (!older.is(ModItems.INFERIUM_UPGRADE.get()) ||
                !machine.getUpgradeInventory().getStackInSlot(0).is(ModItems.SUPREMIUM_UPGRADE.get())) {
            helper.fail("Applying a replacement upgrade lost the old upgrade or selected the wrong new tier");
            return;
        }
        var tag = machine.saveWithFullMetadata();
        var reloaded = new InfuserBlockEntity(machine.getBlockPos(), machine.getBlockState());
        reloaded.load(tag);
        if (reloaded.getOperationTicks() != 2 ||
                reloaded.getEnergyCostPerTick() != 960 ||
                reloaded.getEnergy().getMaxEnergyStored() != 480_000 ||
                !reloaded.getUpgradeInventory().getStackInSlot(0).is(ModItems.SUPREMIUM_UPGRADE.get())) {
            helper.fail("Supremium upgrade and its speed/power/capacity values did not survive NBT reload");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void removingUpgradeClampsEnergy(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.SUPREMIUM_UPGRADE.get()));
        for (int i = 0; i < 140; i++)
            machine.getEnergy().receiveEnergy(1_000, false);
        if (machine.getEnergy().getEnergyStored() != 140_000) {
            helper.fail("Could not store energy beyond the non-upgraded capacity");
            return;
        }
        machine.getUpgradeInventory().setStackInSlot(0, ItemStack.EMPTY);
        if (machine.getEnergy().getMaxEnergyStored() != 80_000 ||
                machine.getEnergy().getEnergyStored() != 80_000) {
            helper.fail("Removing upgrade did not restore 80,000 FE maximum and clamp current power");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void upgradedInfuserActuallyProcesses(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.SUPREMIUM_UPGRADE.get()));
        machine.getInventory().setStackInSlot(0, new ItemStack(ModItems.MASTER_INFUSION_CRYSTAL.get()));
        machine.getInventory().setStackInSlot(4, new ItemStack(ModItems.IMPERIUM_ESSENCE.get(), 4));
        for (int i = 0; i < 2; i++) machine.getEnergy().receiveEnergy(1_000, false);
        helper.runAfterDelay(10, () -> {
            var output = machine.getInventory().getStackInSlot(8);
            if (output.is(ModItems.SUPREMIUM_ESSENCE.get()) &&
                    output.getCount() == 1 &&
                    machine.getInventory().getStackInSlot(4).isEmpty() &&
                    machine.getEnergy().getEnergyStored() == 80) {
                helper.succeed();
            } else {
                helper.fail("Supremium upgrade did not convert essence in 2 ticks consuming 1920 FE");
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void allSixUpgradeTiersMatchOriginal(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var items = new net.minecraft.world.item.Item[] {
            ModItems.INFERIUM_UPGRADE.get(), ModItems.PRUDENTIUM_UPGRADE.get(),
            ModItems.TERTIUM_UPGRADE.get(), ModItems.IMPERIUM_UPGRADE.get(),
            ModItems.SUPREMIUM_UPGRADE.get(), ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()
        };
        int[] expectedTicks = {83, 62, 40, 12, 2, 1};
        int[] expectedEnergy = {40, 80, 120, 320, 960, 1920};
        int[] expectedCapacity = {160_000, 240_000, 320_000, 400_000, 480_000, 560_000};
        for (int i = 0; i < items.length; i++) {
            machine.getUpgradeInventory().setStackInSlot(0, new ItemStack(items[i]));
            if (machine.getOperationTicks() != expectedTicks[i] ||
                    machine.getEnergyCostPerTick() != expectedEnergy[i] ||
                    machine.getEnergy().getMaxEnergyStored() != expectedCapacity[i]) {
                helper.fail("Upgrade tier " + i + " does not match original speed/power/capacity values");
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void essenceInputAccepts512AndRejectsOverflow(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        if (machine.getInventory().getSlotLimit(1) != 512) {
            helper.fail("Inferium staging slot should have a 512-item limit");
            return;
        }
        for (int i = 0; i < 8; i++) {
            var remainder = machine.getInventory().insertItem(1,
                    new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 64), false);
            if (!remainder.isEmpty()) {
                helper.fail("Infuser prematurely rejected an input stack at insertion " + i);
                return;
            }
        }
        var overflow = machine.getInventory().insertItem(1,
                new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 64), false);
        if (machine.getInventory().getStackInSlot(1).getCount() != 512 ||
                overflow.getCount() != 64) {
            helper.fail("Infuser essence storage overflowed or lost items at 512");
            return;
        }
        var saved = machine.saveWithFullMetadata();
        var restored = new InfuserBlockEntity(machine.getBlockPos(), machine.getBlockState());
        restored.load(saved);
        if (restored.getInventory().getStackInSlot(1).getCount() != 512) {
            helper.fail("512-item essence stack not preserved by NBT round-trip");
            return;
        }
        var removed = restored.getInventory().extractItem(1, 64, false);
        if (removed.getCount() != 64 ||
                restored.getInventory().getStackInSlot(1).getCount() != 448) {
            helper.fail("Extracting from 512-input stack lost or duplicated items");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void largeStackNetworkPreviewAndClicks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        machine.getInventory().setStackInSlot(1,
                new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 512));
        InfuserMenu menu = new InfuserMenu(1, player.getInventory(), machine);
        ItemStack preview = menu.slots.get(2).getItem();
        if (preview.getCount() != 1 || menu.getEssenceCount(0) != 512) {
            helper.fail("Infuser inventory preview or 512-count sync is unsafe");
            return;
        }
        var packet = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        packet.writeItem(preview);
        ItemStack received = packet.readItem();
        packet.release();
        if (!received.is(ModItems.INFERIUM_ESSENCE.get()) || received.getCount() != 1) {
            helper.fail("Vanilla packet failed to encode Infuser's safe slot preview");
            return;
        }
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        if (menu.getCarried().getCount() != 64 ||
                machine.getInventory().getStackInSlot(1).getCount() != 448) {
            helper.fail("Left-click failed to safely withdraw a 64-item batch");
            return;
        }
        menu.clicked(2, 1, net.minecraft.world.inventory.ClickType.PICKUP, player);
        if (menu.getCarried().getCount() != 63 ||
                machine.getInventory().getStackInSlot(1).getCount() != 449) {
            helper.fail("Right-click failed to safely return one essence");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void shiftClickLargeBufferIsConservative(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        machine.getInventory().setStackInSlot(1,
                new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 512));
        InfuserMenu menu = new InfuserMenu(1, player.getInventory(), machine);
        menu.quickMoveStack(player, 2);
        int movedIntoPlayer = 0;
        int playerSlot = -1;
        for (int index = 10; index < 46; index++) {
            ItemStack stack = menu.slots.get(index).getItem();
            if (!stack.isEmpty()) {
                movedIntoPlayer += stack.getCount();
                playerSlot = index;
            }
        }
        if (movedIntoPlayer != 64 || machine.getInventory().getStackInSlot(1).getCount() != 448) {
            helper.fail("Shift-click out of 512 buffer duplicated or lost items");
            return;
        }
        menu.quickMoveStack(player, playerSlot);
        if (machine.getInventory().getStackInSlot(1).getCount() != 512) {
            helper.fail("Shift-click input failed to refill 512-item buffer");
            return;
        }
        int leftInPlayer = 0;
        for (int index = 10; index < 46; index++)
            leftInPlayer += menu.slots.get(index).getItem().getCount();
        if (leftInPlayer == 0) helper.succeed();
        else helper.fail("Shift-click refill duplicated essence in player inventory");
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void quickMoveEventAndUnsafeClicksDoNotLoseItems(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        machine.getInventory().setStackInSlot(1,
                new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 512));
        InfuserMenu menu = new InfuserMenu(1, player.getInventory(), machine);
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.SWAP, player);
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.THROW, player);
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.CLONE, player);
        if (machine.getInventory().getStackInSlot(1).getCount() != 512) {
            helper.fail("Unsupported oversized-buffer click modified stored essence");
            return;
        }
        if (menu.canDragTo(menu.slots.get(2)) ||
                menu.canTakeItemForPickAll(
                        new ItemStack(ModItems.INFERIUM_ESSENCE.get()), menu.slots.get(2))) {
            helper.fail("Vanilla drag or pickup-all could access a 512-item buffer");
            return;
        }
        menu.clicked(2, 0, net.minecraft.world.inventory.ClickType.QUICK_MOVE, player);
        int inPlayer = 0;
        for (int i = 10; i < 46; i++)
            inPlayer += menu.slots.get(i).getItem().getCount();
        if (inPlayer == 64 && machine.getInventory().getStackInSlot(1).getCount() == 448)
            helper.succeed();
        else helper.fail("Actual shift-click event did not extract one safe 64-item batch");
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void fullContainerPacketStaysNetworkSafe(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        machine.getInventory().setStackInSlot(1,
                new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 512));
        var player = helper.makeMockPlayer();
        InfuserMenu menu = new InfuserMenu(1, player.getInventory(), machine);
        var snapshots = net.minecraft.core.NonNullList.<ItemStack>create();
        for (var slot : menu.slots) snapshots.add(slot.getItem().copy());
        var outbound = new net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket(
                1, 1, snapshots, ItemStack.EMPTY);
        var wire = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        outbound.write(wire);
        var decoded = new net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket(wire);
        wire.release();
        if (decoded.getItems().size() == 46
                && decoded.getItems().get(2).is(ModItems.INFERIUM_ESSENCE.get())
                && decoded.getItems().get(2).getCount() == 1
                && menu.getEssenceCount(0) == 512) helper.succeed();
        else helper.fail("Whole inventory packet could not safely represent 512-item backing stack");
    }

    @GameTest(template = "empty", timeoutTicks = 55)
    public static void pipeCapabilityFillsAndEmptiesSafely(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.INFUSER.get());
        InfuserBlockEntity machine = (InfuserBlockEntity) helper.getBlockEntity(pos);
        var top = machine.getCapability(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,
                net.minecraft.core.Direction.UP).orElse(null);
        var bottom = machine.getCapability(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,
                net.minecraft.core.Direction.DOWN).orElse(null);
        if (top == null || bottom == null) {
            helper.fail("Missing sided Forge item capability");
            return;
        }
        for (int i = 0; i < 8; i++) {
            if (!top.insertItem(1, new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 64), false).isEmpty()) {
                helper.fail("Top pipe rejected an essence stack before reaching 512");
                return;
            }
        }
        var rejected = top.insertItem(1, new ItemStack(ModItems.INFERIUM_ESSENCE.get(), 64), false);
        if (rejected.getCount() != 64 ||
                machine.getInventory().getStackInSlot(1).getCount() != 512 ||
                !bottom.extractItem(1, 64, false).isEmpty()) {
            helper.fail("Pipe overflow or forbidden downward input extraction");
            return;
        }
        machine.getInventory().setStackInSlot(8,
                new ItemStack(ModItems.SUPREMIUM_ESSENCE.get(), 3));
        if (bottom.extractItem(8, 3, false).getCount() != 3 ||
                !machine.getInventory().getStackInSlot(8).isEmpty()) {
            helper.fail("Output pipe failed to safely drain completed essence");
            return;
        }
        helper.succeed();
    }

    private InfuserGameTests() {}
}

package com.blakebr0.mysticalautomation.gametest;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.tileentity.CrafterBlockEntity;
import com.blakebr0.mysticalagriculture.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder("mysticalautomation")
public final class CrafterGameTests {
    private static CrafterBlockEntity machine(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.CRAFTER.get());
        return (CrafterBlockEntity) helper.getBlockEntity(relative);
    }
    private static void configureTableRecipe(CrafterBlockEntity machine) {
        for (int i : new int[]{0, 1, 3, 4}) {
            machine.setRecipePatternSlot(i, new ItemStack(Items.OAK_PLANKS));
        }
    }

    @GameTest(template = "empty", timeoutTicks = 280)
    public static void craftsVanillaTableFromPooledInputs(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        configureTableRecipe(machine);
        machine.getInventory().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
        for (int i = 0; i < 5; i++) machine.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(212, () -> {
            if (machine.getInventory().getStackInSlot(10).is(Items.CRAFTING_TABLE) &&
                    machine.getInventory().getStackInSlot(10).getCount() == 1 &&
                    machine.getInventory().getStackInSlot(0).isEmpty())
                helper.succeed();
            else helper.fail("Crafter failed to consume four pooled planks and produce a crafting table: " + machine.debugStatus());
        });
    }

    @GameTest(template = "empty", timeoutTicks = 270)
    public static void noIngredientsNoCraft(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        configureTableRecipe(machine);
        for (int i = 0; i < 5; i++) machine.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(212, () -> {
            if (machine.getInventory().getStackInSlot(10).isEmpty()) helper.succeed();
            else helper.fail("Crafter produced an item without recipe inputs");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 270)
    public static void fullOutputPreservesInputs(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        configureTableRecipe(machine);
        machine.getInventory().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
        machine.getInventory().setStackInSlot(10, new ItemStack(Items.CRAFTING_TABLE, 64));
        for (int i = 0; i < 5; i++) machine.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(212, () -> {
            if (machine.getInventory().getStackInSlot(10).getCount() == 64 &&
                    machine.getInventory().getStackInSlot(0).getCount() == 4) helper.succeed();
            else helper.fail("Crafter ate inputs or overflowed its output inventory");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 45)
    public static void recipeBlueprintPersists(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        configureTableRecipe(machine);
        machine.getInventory().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 7));
        var saved = machine.saveWithFullMetadata();
        var restored = new CrafterBlockEntity(machine.getBlockPos(), machine.getBlockState());
        restored.load(saved);
        if (restored.getRecipePattern().getStackInSlot(4).is(Items.OAK_PLANKS) &&
                restored.getInventory().getStackInSlot(0).getCount() == 7) helper.succeed();
        else helper.fail("Crafter recipe template and item stock didn't survive save/load");
    }

    @GameTest(template = "empty", timeoutTicks = 70)
    public static void upgradeAcceleratesCraft(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        configureTableRecipe(machine);
        machine.getInventory().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()));
        machine.getEnergy().receiveEnergy(2000, false);
        helper.runAfterDelay(7, () -> {
            if (machine.getInventory().getStackInSlot(10).is(Items.CRAFTING_TABLE) &&
                    machine.getOperationTicks() == 1 &&
                    machine.getEnergyCostPerTick() == 1920) helper.succeed();
            else helper.fail("Awakened Supremium upgrade failed to accelerate Crafter recipe: " + machine.debugStatus());
        });
    }

    @GameTest(template = "empty", timeoutTicks = 45)
    public static void recipeRegistered(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(
                new ResourceLocation("mysticalautomation", "crafter"));
        if (recipe.isPresent()) helper.succeed();
        else helper.fail("Mystical Automation Crafter crafting recipe missing");
    }
    @GameTest(template = "empty", timeoutTicks = 285)
    public static void honeyBottleCraftReturnsGlassBottles(GameTestHelper helper) {
        CrafterBlockEntity machine = machine(helper);
        // Four honey bottles form a honey block; each must leave its bottle.
        int[] pattern = {0, 1, 3, 4};
        for (int slot : pattern) {
            machine.setRecipePatternSlot(slot, new ItemStack(Items.HONEY_BOTTLE));
        }
        for (int i = 0; i < 4; i++)
            machine.getInventory().setStackInSlot(i, new ItemStack(Items.HONEY_BOTTLE));
        for (int i = 0; i < 5; i++)
            machine.getEnergy().receiveEnergy(1000, false);
        helper.runAfterDelay(212, () -> {
            if (!machine.getInventory().getStackInSlot(10).is(Items.HONEY_BLOCK)) {
                helper.fail("Crafter did not produce honey block; " + machine.debugStatus());
                return;
            }
            for (int i = 0; i < 4; i++) {
                if (!machine.getInventory().getStackInSlot(i).is(Items.GLASS_BOTTLE)) {
                    helper.fail("Crafter lost or misplaced a glass bottle at input " + i);
                    return;
                }
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 45)
    public static void ghostMenuDoesNotConsumeTemplateItem(GameTestHelper helper) {
        var machine = machine(helper);
        var player = helper.makeMockPlayer();
        var menu = new com.blakebr0.mysticalautomation.container.CrafterMenu(1,
                player.getInventory(), machine);
        if (menu.slots.size() != 57) {
            helper.fail("Crafter menu slot count incorrect");
            return;
        }
        menu.setCarried(new ItemStack(Items.OAK_PLANKS, 32));
        menu.clicked(12, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        if (!machine.getRecipePattern().getStackInSlot(0).is(Items.OAK_PLANKS)
                || menu.getCarried().getCount() != 32) {
            helper.fail("Ghost recipe click consumed item from cursor");
            return;
        }
        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(12, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        if (machine.getRecipePattern().getStackInSlot(0).isEmpty()) helper.succeed();
        else helper.fail("Ghost recipe cell could not be cleared");
    }

    @GameTest(template = "empty", timeoutTicks = 45)
    public static void shiftClickInputsAndSyncedUpgrades(GameTestHelper helper) {
        var machine = machine(helper);
        configureTableRecipe(machine);
        var player = helper.makeMockPlayer();
        var menu = new com.blakebr0.mysticalautomation.container.CrafterMenu(1,
                player.getInventory(), machine);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 8));
        menu.quickMoveStack(player, 21);
        if (machine.getInventory().getStackInSlot(0).getCount() != 8
                || !player.getInventory().getItem(9).isEmpty()) {
            helper.fail("Shift-click did not move the resource to matching input");
            return;
        }
        machine.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()));
        if (menu.getMaximumEnergy() == 560000 && menu.getOperationTicks() == 1)
            helper.succeed();
        else helper.fail("Upgraded Crafter menu power or timing not synced");
    }

    private CrafterGameTests() {}
}

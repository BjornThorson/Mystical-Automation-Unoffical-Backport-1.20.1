package com.blakebr0.mysticalautomation.gametest;

import com.blakebr0.mysticalautomation.tileentity.FarmerBlockEntity;
import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModFarmerRecipes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder("mysticalautomation")
public final class FarmerGameTests {
    private static FarmerBlockEntity make(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.FARMER.get());
        return (FarmerBlockEntity) helper.getBlockEntity(pos);
    }
    private static boolean inputs(FarmerBlockEntity farmer) {
        var soil = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("mysticalagriculture", "inferium_farmland"));
        if (soil == null || soil == Items.AIR) return false;
        farmer.getInventory().setStackInSlot(0, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("mysticalagriculture", "inferium_seeds"))));
        farmer.getInventory().setStackInSlot(1, new ItemStack(soil));
        return true;
    }
    private static void fastAndPowered(FarmerBlockEntity farmer) {
        farmer.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()));
        for (int i = 0; i < 100; i++)
            farmer.getEnergy().receiveEnergy(3840, false);
    }
    /** Farmer UI slots, upgrade data and the full 32-bit FE value are server-backed. */
    @GameTest(template="empty", timeoutTicks=50)
    public static void farmerMenuLayoutAndSync(GameTestHelper helper) {
        FarmerBlockEntity machine = make(helper);
        var player = helper.makeMockPlayer();
        var menu = new com.blakebr0.mysticalautomation.container.FarmerMenu(
                1, player.getInventory(), machine);
        if (menu.slots.size() != 50 || menu.getMaximumEnergy() != 80_000
                || menu.getOperationTicks() != 800 || menu.getStages() != 0
                || menu.getSlot(0).x != 152 || menu.getSlot(1).x != 60
                || menu.getSlot(4).x != 30 || menu.getSlot(5).x != 116
                || menu.getSlot(14).x != 8) {
            helper.fail("Farmer GUI original slot geometry or data mismatch");
            return;
        }
        // FE receive is capped per call, just as with external input.
        for (int i = 0; i < 80; i++)
            machine.getEnergy().receiveEnergy(1000, false);
        if (menu.getEnergy() != 80_000 || menu.getMaximumEnergy() != 80_000
                || menu.getSlot(5).mayPlace(new ItemStack(Items.WHEAT))
                || !menu.getSlot(4).mayPlace(new ItemStack(Items.COAL))
                || menu.getSlot(4).mayPlace(new ItemStack(Items.DIRT))) {
            helper.fail("Farmer FE/slots: energy=" + menu.getEnergy()
                    + ", max=" + menu.getMaximumEnergy()
                    + ", outputMayPlace=" + menu.getSlot(5).mayPlace(new ItemStack(Items.WHEAT))
                    + ", coalMayPlace=" + menu.getSlot(4).mayPlace(new ItemStack(Items.COAL))
                    + ", dirtMayPlace=" + menu.getSlot(4).mayPlace(new ItemStack(Items.DIRT)));
            return;
        }
        machine.getInventory().setStackInSlot(4, new ItemStack(Items.WHEAT, 7));
        menu.quickMoveStack(player, 5);
        if (!machine.getInventory().getStackInSlot(4).isEmpty()
                || !player.getInventory().getItem(8).is(Items.WHEAT)
                || player.getInventory().getItem(8).getCount() != 7) {
            helper.fail("Farmer GUI could not safely quick-move a harvest output");
            return;
        }
        helper.succeed();
    }

    @GameTest(template="empty", timeoutTicks=50)
    public static void upstreamFarmerRecipesRegistered(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().byKey(
                new ResourceLocation("mysticalautomation", "farmer/inferium_seeds_tier_1"));
        if (recipe.isPresent() && recipe.get().getType() == ModFarmerRecipes.TYPE.get())
            helper.succeed();
        else helper.fail("Farmer custom recipe and type not loaded");
    }
    @GameTest(template="empty", timeoutTicks=100)
    public static void harvestingProducesEssenceWithoutConsumingInputs(GameTestHelper helper) {
        FarmerBlockEntity farmer = make(helper);
        if (!inputs(farmer)) { helper.fail("Missing 1.20.1 inferium farmland item"); return; }
        fastAndPowered(farmer);
        helper.runAfterDelay(50, () -> {
            ItemStack output = farmer.getInventory().getStackInSlot(4);
            if (output.is(ModItems.INFERIUM_ESSENCE.get()) &&
                    farmer.getInventory().getStackInSlot(0).is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("mysticalagriculture", "inferium_seeds"))) &&
                    !farmer.getInventory().getStackInSlot(1).isEmpty())
                helper.succeed();
            else helper.fail("Farmer did not produce inferium or consumed reusable seed/soil inputs");
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void unpoweredFarmerNeverHarvests(GameTestHelper helper) {
        FarmerBlockEntity farmer = make(helper);
        if (!inputs(farmer)) { helper.fail("Missing 1.20.1 farmland item"); return; }
        farmer.getUpgradeInventory().setStackInSlot(0,
                new ItemStack(ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()));
        helper.runAfterDelay(55, () -> {
            if (farmer.getInventory().getStackInSlot(4).isEmpty() &&
                farmer.getStageProgress() == 0) helper.succeed();
            else helper.fail("Farmer generated a harvest without FE");
        });
    }
    @GameTest(template="empty", timeoutTicks=90)
    public static void fullOutputNeverDeletesHarvest(GameTestHelper helper) {
        FarmerBlockEntity farmer = make(helper);
        if (!inputs(farmer)) { helper.fail("Missing 1.20.1 farmland item"); return; }
        for (int i = 4; i <= 12; i++)
            farmer.getInventory().setStackInSlot(i, new ItemStack(Items.COBBLESTONE, 64));
        fastAndPowered(farmer);
        helper.runAfterDelay(52, () -> {
            boolean intact = true;
            for (int i = 4; i <= 12; i++)
                intact &= farmer.getInventory().getStackInSlot(i).is(Items.COBBLESTONE) &&
                        farmer.getInventory().getStackInSlot(i).getCount() == 64;
            if (intact && farmer.getInventory().getStackInSlot(0).is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("mysticalagriculture", "inferium_seeds"))))
                helper.succeed();
            else helper.fail("Farmer overflowed a full output inventory or deleted seeds");
        });
    }
    @GameTest(template="empty", timeoutTicks=50)
    public static void growthAndUpgradeSurviveNbt(GameTestHelper helper) {
        FarmerBlockEntity farmer = make(helper);
        if (!inputs(farmer)) { helper.fail("Missing 1.20.1 farmland item"); return; }
        fastAndPowered(farmer);
        helper.runAfterDelay(16, () -> {
            var saved = farmer.saveWithFullMetadata();
            var restored = new FarmerBlockEntity(farmer.getBlockPos(), farmer.getBlockState());
            restored.load(saved);
            if (restored.getStageProgress() == farmer.getStageProgress() &&
                    restored.getEnergy().getEnergyStored() == farmer.getEnergy().getEnergyStored() &&
                    restored.getUpgradeInventory().getStackInSlot(0).is(
                            ModItems.AWAKENED_SUPREMIUM_UPGRADE.get()))
                helper.succeed();
            else helper.fail("Farmer energy/stages/upgrades lost during NBT round-trip");
        });
    }
    @GameTest(template="empty", timeoutTicks=50)
    public static void allSeventeenFarmerRecipesLoaded(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(ModFarmerRecipes.TYPE.get());
        var farmer = make(helper);
        if (farmer.getInventory().getSlots() == 16 && recipes.size() == 17) helper.succeed();
        else helper.fail("Expected 16 original Farmer inventory slots and 17 loaded recipes; got " +
                farmer.getInventory().getSlots() + " slots and " + recipes.size() + " recipes");
    }

    @GameTest(template="empty", timeoutTicks=45)
    public static void farmerItemCraftRecipeRegistered(GameTestHelper helper) {
        var itemRecipe = helper.getLevel().getRecipeManager().byKey(
                new ResourceLocation("mysticalautomation", "farmer"));
        if (itemRecipe.isPresent()) helper.succeed();
        else helper.fail("Farmer machine crafting recipe absent in 1.20.1");
    }

    private FarmerGameTests() {}
}

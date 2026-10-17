package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.block.MachineShellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Preserve all seven original registry names. Each machine's inventory slot
 * count matches upstream 1.21.1 (upgrade inventory is separately tracked).
 */
public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MysticalAutomation.MOD_ID);

    public static final RegistryObject<Block> INFUSER =
            REGISTRY.register("infuser", () -> new MachineShellBlock(ModBlockEntities.INFUSER, 9));
    public static final RegistryObject<Block> CRAFTER =
            REGISTRY.register("crafter", () -> new MachineShellBlock(ModBlockEntities.CRAFTER, 11));
    public static final RegistryObject<Block> FARMER =
            REGISTRY.register("farmer", () -> new MachineShellBlock(ModBlockEntities.FARMER, 16));
    public static final RegistryObject<Block> FERTILIZER =
            REGISTRY.register("fertilizer", () -> new MachineShellBlock(ModBlockEntities.FERTILIZER, 9));
    public static final RegistryObject<Block> INFUSION_ALTARNATOR =
            REGISTRY.register("infusion_altarnator", () -> new MachineShellBlock(ModBlockEntities.INFUSION_ALTARNATOR, 11));
    public static final RegistryObject<Block> AWAKENING_ALTARNATOR =
            REGISTRY.register("awakening_altarnator", () -> new MachineShellBlock(ModBlockEntities.AWAKENING_ALTARNATOR, 11));
    public static final RegistryObject<Block> ENCHANTERNATOR =
            REGISTRY.register("enchanternator", () -> new MachineShellBlock(ModBlockEntities.ENCHANTERNATOR, 5));

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
    private ModBlocks() {}
}

package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.tileentity.MachineShellBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.InfuserBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.CrafterBlockEntity;
import com.blakebr0.mysticalautomation.tileentity.FarmerBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Stable upstream block entity registry names; the implementation is
 * temporary until each concrete machine is ported.
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MysticalAutomation.MOD_ID);

    public static final RegistryObject<BlockEntityType<InfuserBlockEntity>> INFUSER =
            REGISTRY.register("infuser", () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, ModBlocks.INFUSER.get()).build(null));
    public static final RegistryObject<BlockEntityType<CrafterBlockEntity>> CRAFTER =
            REGISTRY.register("crafter", () -> BlockEntityType.Builder.of(CrafterBlockEntity::new, ModBlocks.CRAFTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FarmerBlockEntity>> FARMER =
            REGISTRY.register("farmer", () -> BlockEntityType.Builder.of(FarmerBlockEntity::new, ModBlocks.FARMER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MachineShellBlockEntity>> FERTILIZER =
            register("fertilizer", ModBlocks.FERTILIZER);
    public static final RegistryObject<BlockEntityType<MachineShellBlockEntity>> INFUSION_ALTARNATOR =
            register("infusion_altarnator", ModBlocks.INFUSION_ALTARNATOR);
    public static final RegistryObject<BlockEntityType<MachineShellBlockEntity>> AWAKENING_ALTARNATOR =
            register("awakening_altarnator", ModBlocks.AWAKENING_ALTARNATOR);
    public static final RegistryObject<BlockEntityType<MachineShellBlockEntity>> ENCHANTERNATOR =
            register("enchanternator", ModBlocks.ENCHANTERNATOR);

    private static RegistryObject<BlockEntityType<MachineShellBlockEntity>> register(String name, Supplier<Block> block) {
        return REGISTRY.register(name, () ->
                BlockEntityType.Builder.of(MachineShellBlockEntity::new, block.get()).build(null));
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private ModBlockEntities() {}
}

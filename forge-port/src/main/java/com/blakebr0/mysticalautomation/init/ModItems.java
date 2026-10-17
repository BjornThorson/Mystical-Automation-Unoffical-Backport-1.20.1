package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Machine block items; recipes and creative tabs follow their functional ports. */
public final class ModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, MysticalAutomation.MOD_ID);

    public static final RegistryObject<Item> INFUSER = blockItem("infuser", ModBlocks.INFUSER);
    public static final RegistryObject<Item> CRAFTER = blockItem("crafter", ModBlocks.CRAFTER);
    public static final RegistryObject<Item> FARMER = blockItem("farmer", ModBlocks.FARMER);
    public static final RegistryObject<Item> FERTILIZER = blockItem("fertilizer", ModBlocks.FERTILIZER);
    public static final RegistryObject<Item> INFUSION_ALTARNATOR = blockItem("infusion_altarnator", ModBlocks.INFUSION_ALTARNATOR);
    public static final RegistryObject<Item> AWAKENING_ALTARNATOR = blockItem("awakening_altarnator", ModBlocks.AWAKENING_ALTARNATOR);
    public static final RegistryObject<Item> ENCHANTERNATOR = blockItem("enchanternator", ModBlocks.ENCHANTERNATOR);

    private static RegistryObject<Item> blockItem(String name, Supplier<? extends Block> block) {
        return REGISTRY.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
    private ModItems() {}
}

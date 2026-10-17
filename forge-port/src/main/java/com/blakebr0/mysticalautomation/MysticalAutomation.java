package com.blakebr0.mysticalautomation;

import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModBlockEntities;
import com.blakebr0.mysticalautomation.init.ModItems;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.init.ModFarmerRecipes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Unofficial full backport of BlakeBr0's Mystical Automation.
 * Development build: seven block identities, not yet seven working machines.
 */
@Mod(MysticalAutomation.MOD_ID)
public final class MysticalAutomation {
    public static final String MOD_ID = "mysticalautomation";

    public MysticalAutomation() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModFarmerRecipes.register(modBus);
    }
}

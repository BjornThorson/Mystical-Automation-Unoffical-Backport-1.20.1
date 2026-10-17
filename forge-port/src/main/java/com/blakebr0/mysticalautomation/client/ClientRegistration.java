package com.blakebr0.mysticalautomation.client;

import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.client.screen.InfuserScreen;
import com.blakebr0.mysticalautomation.client.screen.CrafterScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "mysticalautomation", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRegistration {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.INFUSER.get(), InfuserScreen::new);
            MenuScreens.register(ModMenuTypes.CRAFTER.get(), CrafterScreen::new);
        });
    }
    private ClientRegistration() {}
}

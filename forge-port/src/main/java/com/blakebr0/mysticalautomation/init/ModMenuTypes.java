package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.container.InfuserMenu;
import com.blakebr0.mysticalautomation.container.CrafterMenu;
import com.blakebr0.mysticalautomation.container.FarmerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MysticalAutomation.MOD_ID);
    public static final RegistryObject<MenuType<InfuserMenu>> INFUSER =
            REGISTRY.register("infuser", () -> IForgeMenuType.create(InfuserMenu::new));
    public static final RegistryObject<MenuType<CrafterMenu>> CRAFTER =
            REGISTRY.register("crafter", () -> IForgeMenuType.create(CrafterMenu::new));
    public static final RegistryObject<MenuType<FarmerMenu>> FARMER =
            REGISTRY.register("farmer", () -> IForgeMenuType.create(FarmerMenu::new));
    public static void register(IEventBus bus) { REGISTRY.register(bus); }
    private ModMenuTypes() {}
}

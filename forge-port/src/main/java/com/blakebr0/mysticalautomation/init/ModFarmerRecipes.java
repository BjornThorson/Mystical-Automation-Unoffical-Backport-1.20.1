package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.crafting.FarmerRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge 1.20.1 conversion of the upstream Mystical Automation Farmer recipe type. */
public final class ModFarmerRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MysticalAutomation.MOD_ID);
    public static final RegistryObject<RecipeSerializer<FarmerRecipe>> SERIALIZER =
            SERIALIZERS.register("farmer", FarmerRecipe.Serializer::new);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MysticalAutomation.MOD_ID);
    public static final RegistryObject<RecipeType<FarmerRecipe>> TYPE =
            TYPES.register("farmer", () -> new RecipeType<>() {
                @Override public String toString() { return "mysticalautomation:farmer"; }
            });
    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
    private ModFarmerRecipes() {}
}

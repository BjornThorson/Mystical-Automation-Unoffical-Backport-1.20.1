package com.blakebr0.mysticalautomation.crafting;

import com.blakebr0.mysticalautomation.init.ModFarmerRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Farmer recipe reconstructed for Forge 1.20.1 from the upstream MIT recipe format. */
public final class FarmerRecipe implements Recipe<Container> {
    public record Drop(ItemStack item, float chance) {}

    private final ResourceLocation id;
    private final Ingredient seeds, soil, crux;
    private final int stages;
    private final List<Drop> drops;

    public FarmerRecipe(ResourceLocation id, Ingredient seeds, Ingredient soil, Ingredient crux,
                        int stages, List<Drop> drops) {
        this.id = id;
        this.seeds = seeds;
        this.soil = soil;
        this.crux = crux;
        this.stages = stages;
        this.drops = List.copyOf(drops);
    }

    @Override public boolean matches(Container grid, Level level) {
        if (grid.getContainerSize() < 3) return false;
        return seeds.test(grid.getItem(0)) && soil.test(grid.getItem(1))
                && (crux.isEmpty() ? grid.getItem(2).isEmpty() : crux.test(grid.getItem(2)));
    }

    public int getStages() { return stages; }
    public List<Drop> getDrops() { return drops; }

    public List<ItemStack> rollResults(RandomSource random) {
        List<ItemStack> outcome = new ArrayList<>();
        for (Drop drop : drops)
            if (random.nextFloat() < drop.chance())
                outcome.add(drop.item().copy());
        return outcome;
    }

    @Override public ItemStack assemble(Container container, RegistryAccess registries) {
        return drops.isEmpty() ? ItemStack.EMPTY : drops.get(0).item().copy();
    }
    @Override public ItemStack getResultItem(RegistryAccess registries) {
        return drops.isEmpty() ? ItemStack.EMPTY : drops.get(0).item().copy();
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 3; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModFarmerRecipes.SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModFarmerRecipes.TYPE.get(); }
    @Override public boolean isSpecial() { return true; }

    public static final class Serializer implements RecipeSerializer<FarmerRecipe> {
        @Override public FarmerRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient seeds = Ingredient.fromJson(json.get("seeds"));
            Ingredient soil = Ingredient.fromJson(json.get("soil"));
            Ingredient crux = json.has("crux") ?
                    Ingredient.fromJson(json.get("crux")) : Ingredient.EMPTY;
            int stages = GsonHelper.getAsInt(json, "stages", 7);
            JsonArray outcomes = GsonHelper.getAsJsonArray(json, "results");
            List<Drop> drops = new ArrayList<>();
            for (var element : outcomes) {
                JsonObject raw = element.getAsJsonObject();
                JsonObject entry = GsonHelper.getAsJsonObject(raw, "item");
                ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(entry, "id"));
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null || item == net.minecraft.world.item.Items.AIR)
                    throw new com.google.gson.JsonParseException("Unknown Farmer result item: " + itemId);
                ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(entry, "count", 1));
                float chance = GsonHelper.getAsFloat(raw, "chance", 1.0f);
                if (chance < 0 || chance > 1 || stages < 1)
                    throw new com.google.gson.JsonParseException("Invalid Farmer recipe chance/stages: " + id);
                drops.add(new Drop(stack, chance));
            }
            if (drops.isEmpty()) throw new com.google.gson.JsonParseException("Empty Farmer output: " + id);
            return new FarmerRecipe(id, seeds, soil, crux, stages, drops);
        }

        @Override public @Nullable FarmerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient seeds = Ingredient.fromNetwork(buffer);
            Ingredient soil = Ingredient.fromNetwork(buffer);
            Ingredient crux = Ingredient.fromNetwork(buffer);
            int stages = buffer.readVarInt();
            int count = buffer.readVarInt();
            List<Drop> results = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ItemStack item = buffer.readItem();
                float chance = buffer.readFloat();
                results.add(new Drop(item, chance));
            }
            return new FarmerRecipe(id, seeds, soil, crux, stages, results);
        }

        @Override public void toNetwork(FriendlyByteBuf buffer, FarmerRecipe recipe) {
            recipe.seeds.toNetwork(buffer);
            recipe.soil.toNetwork(buffer);
            recipe.crux.toNetwork(buffer);
            buffer.writeVarInt(recipe.stages);
            buffer.writeVarInt(recipe.drops.size());
            for (Drop drop : recipe.drops) {
                buffer.writeItem(drop.item());
                buffer.writeFloat(drop.chance());
            }
        }
    }
}

package com.youtyan.apoex.client.jei;

import com.youtyan.apoex.ApoEXMod;
import com.youtyan.apoex.client.jei.category.ApoExSmithingCategory;
import com.youtyan.apoex.client.jei.category.GemSmithingCategory;
import com.youtyan.apoex.recipe.ApoExAddSocketRecipe;
import com.youtyan.apoex.recipe.ApoExSmithingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class ApoExJEI implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(ApoEXMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new ApoExSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new GemSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<ApoExAddSocketRecipe> socketRecipes = recipeManager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream()
                .filter(ApoExAddSocketRecipe.class::isInstance)
                .map(ApoExAddSocketRecipe.class::cast)
                .collect(Collectors.toList());
        reg.addRecipes(ApoExSmithingCategory.TYPE, socketRecipes);

        List<ApoExSmithingRecipe> smithingRecipes = recipeManager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream()
                .filter(ApoExSmithingRecipe.class::isInstance)
                .map(ApoExSmithingRecipe.class::cast)
                .collect(Collectors.toList());
        reg.addRecipes(GemSmithingCategory.TYPE, smithingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), ApoExSmithingCategory.TYPE);
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), GemSmithingCategory.TYPE);
    }
}
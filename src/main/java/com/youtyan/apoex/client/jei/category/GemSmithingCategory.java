package com.youtyan.apoex.client.jei.category;

import com.youtyan.apoex.ApoEXMod;
import com.youtyan.apoex.recipe.ApoExSmithingRecipe;
import com.youtyan.apoex.util.ApoExSocketHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;

public class GemSmithingCategory implements IRecipeCategory<ApoExSmithingRecipe> {

    public static final RecipeType<ApoExSmithingRecipe> TYPE = RecipeType.create(ApoEXMod.MODID, "gem_smithing", ApoExSmithingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public GemSmithingCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(new ResourceLocation("apoex", "textures/gui/jei/smithing_table_apoex_jei.png"), 0, 0, 108, 18)
                .setTextureSize(108, 18)
                .addPadding(0, 16, 16, 16)
                .build();
        this.icon = helper.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
        this.title = Component.translatable("category.apoex.gem_smithing");
    }

    @Override
    public RecipeType<ApoExSmithingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ApoExSmithingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 1).addIngredients(recipe.getTemplate());

        List<ItemStack> baseInputs = Arrays.stream(recipe.getBase().getItems())
                .map(ItemStack::copy)
                .peek(ApoExSocketHelper::addSocket)
                .toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 35, 1).addItemStacks(baseInputs);

        builder.addSlot(RecipeIngredientRole.INPUT, 53, 1).addIngredients(recipe.getAddition());

        List<ItemStack> outputs = Arrays.stream(recipe.getBase().getItems())
                .map(ItemStack::copy)
                .peek(ApoExSocketHelper::addSocket)
                .peek(s -> {
                    if (recipe.getAddition().getItems().length > 0) {
                        ApoExSocketHelper.setGem(s, recipe.getAddition().getItems()[0].copy());
                    }
                })
                .toList();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 107, 1).addItemStacks(outputs);
    }

    @Override
    public void draw(ApoExSmithingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        Component text = Component.translatable("text.apoex.set_gem");
        Font font = Minecraft.getInstance().font;
        gfx.drawString(font, text, (108 - font.width(text)) / 2, 23, 0, false);
    }
}

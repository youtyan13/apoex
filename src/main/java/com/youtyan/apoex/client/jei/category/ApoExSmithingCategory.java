package com.youtyan.apoex.client.jei.category;

import com.youtyan.apoex.ApoEXMod;
import com.youtyan.apoex.recipe.ApoExAddSocketRecipe;
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

public class ApoExSmithingCategory implements IRecipeCategory<ApoExAddSocketRecipe> {

    public static final RecipeType<ApoExAddSocketRecipe> TYPE = RecipeType.create(ApoEXMod.MODID, "socket_adding", ApoExAddSocketRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public ApoExSmithingCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(new ResourceLocation("apoex", "textures/gui/jei/smithing_table_apoex_jei.png"), 0, 0, 108, 18)
                .setTextureSize(108, 18)
                .addPadding(0, 16, 16, 16)
                .build();
        this.icon = helper.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
        this.title = Component.translatable("category.apoex.socket_adding");
    }

    @Override
    public RecipeType<ApoExAddSocketRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, ApoExAddSocketRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 1).addIngredients(recipe.getTemplate());
        builder.addSlot(RecipeIngredientRole.INPUT, 35, 1).addIngredients(recipe.getBase());
        builder.addSlot(RecipeIngredientRole.INPUT, 53, 1).addIngredients(recipe.getAddition());

        List<ItemStack> outputs = Arrays.stream(recipe.getBase().getItems())
                .map(ItemStack::copy)
                .peek(ApoExSocketHelper::addSocket)
                .toList();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 107, 1).addItemStacks(outputs);
    }

    @Override
    public void draw(ApoExAddSocketRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        Component text = Component.translatable("text.apoex.add_socket");
        Font font = Minecraft.getInstance().font;
        gfx.drawString(font, text, (108 - font.width(text)) / 2, 23, 0, false);
    }
}

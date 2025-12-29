package com.youtyan.apoex.recipe;

import com.google.gson.JsonObject;
import com.youtyan.apoex.ApoEXMod;
import com.youtyan.apoex.util.ApoExSocketHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

public class ApoExAddSocketRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public ApoExAddSocketRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return this.template.test(container.getItem(0)) &&
                this.base.test(container.getItem(1)) &&
                this.addition.test(container.getItem(2)) &&
                !ApoExSocketHelper.hasSocket(container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack baseStack = container.getItem(1).copy();

        ApoExSocketHelper.addSocket(baseStack);

        return baseStack;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }
    @Override public ResourceLocation getId() { return this.id; }
    @Override public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override public boolean isTemplateIngredient(ItemStack stack) { return this.template.test(stack); }
    @Override public boolean isBaseIngredient(ItemStack stack) { return this.base.test(stack); }
    @Override public boolean isAdditionIngredient(ItemStack stack) { return this.addition.test(stack); }

    public static class Serializer implements RecipeSerializer<ApoExAddSocketRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(ApoEXMod.MODID, "ex_socket_adding");

        @Override
        public ApoExAddSocketRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));
            Ingredient base = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));
            return new ApoExAddSocketRecipe(id, template, base, addition);
        }

        @Override
        public ApoExAddSocketRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient template = Ingredient.fromNetwork(buf);
            Ingredient base = Ingredient.fromNetwork(buf);
            Ingredient addition = Ingredient.fromNetwork(buf);
            return new ApoExAddSocketRecipe(id, template, base, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ApoExAddSocketRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
        }
    }
}

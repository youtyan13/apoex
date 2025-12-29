package com.youtyan.apoex.recipe;

import com.google.gson.JsonObject;
import com.youtyan.apoex.item.IExGem;
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

public class ApoExSmithingRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public ApoExSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack baseStack = container.getItem(1);
        ItemStack additionStack = container.getItem(2);

        boolean basicMatch = this.template.test(container.getItem(0)) &&
                this.base.test(baseStack) &&
                this.addition.test(additionStack) &&
                ApoExSocketHelper.hasSocket(baseStack) &&
                !ApoExSocketHelper.hasGem(baseStack);

        if (!basicMatch) {
            return false;
        }

        if (additionStack.getItem() instanceof IExGem exGem) {
            if (!exGem.canApplyTo(baseStack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack baseStack = container.getItem(1).copy();
        ItemStack gemStack = container.getItem(2);
        if (gemStack.getItem() instanceof IExGem exGem && !exGem.canApplyTo(baseStack)) {
            return ItemStack.EMPTY;
        }

        if (ApoExSocketHelper.hasSocket(baseStack) && !ApoExSocketHelper.hasGem(baseStack)) {
            ApoExSocketHelper.setGem(baseStack, gemStack);

            if (!ApoExSocketHelper.hasGem(baseStack)) {
                return ItemStack.EMPTY;
            }
            
            return baseStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    public static class Serializer implements RecipeSerializer<ApoExSmithingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ApoExSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));
            Ingredient base = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));
            return new ApoExSmithingRecipe(id, template, base, addition);
        }

        @Override
        public ApoExSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient template = Ingredient.fromNetwork(buf);
            Ingredient base = Ingredient.fromNetwork(buf);
            Ingredient addition = Ingredient.fromNetwork(buf);
            return new ApoExSmithingRecipe(id, template, base, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ApoExSmithingRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
        }
    }
}
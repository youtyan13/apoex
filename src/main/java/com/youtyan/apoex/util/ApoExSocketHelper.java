package com.youtyan.apoex.util;

import com.youtyan.apoex.item.IExGem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ApoExSocketHelper {

    private static final String TAG_SOCKET = "apoex_special_socket";
    private static final String TAG_GEM = "apoex_special_gem";

    public static boolean hasSocket(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_SOCKET);
    }

    public static void addSocket(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TAG_SOCKET, true);
    }

    public static boolean hasGem(ItemStack stack) {
        return hasSocket(stack) && stack.getTag().contains(TAG_GEM);
    }

    public static ItemStack getGem(ItemStack stack) {
        if (hasGem(stack)) {
            CompoundTag gemTag = stack.getTag().getCompound(TAG_GEM);
            return ItemStack.of(gemTag);
        }
        return ItemStack.EMPTY;
    }

    public static void setGem(ItemStack stack, ItemStack gem) {
        if (!hasSocket(stack)) return;

        if (gem.getItem() instanceof IExGem specialGem) {
            if (!specialGem.canApplyTo(stack)) {
                return;
            }
        }

        CompoundTag gemTag = new CompoundTag();
        gem.save(gemTag);
        stack.getOrCreateTag().put(TAG_GEM, gemTag);

        if (gem.getItem() instanceof IExGem specialGem) {
            specialGem.onApplied(stack);
        }
    }

    public static boolean hasGem(ItemStack stack, String gemId) {
        if (!hasGem(stack)) return false;
        ItemStack gem = getGem(stack);
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(gem.getItem());
        return id != null && id.toString().equals(gemId);
    }
}
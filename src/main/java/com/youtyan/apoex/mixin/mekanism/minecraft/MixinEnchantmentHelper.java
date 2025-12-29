package com.youtyan.apoex.mixin.mekanism.minecraft;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void injectGetItemEnchantmentLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (MekSuitGemIntegration.isMekArmor(stack)) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND)) {
                CompoundTag mekData = tag.getCompound(NBTConstants.MEK_DATA);
                if (mekData.contains(NBTConstants.ENCHANTMENTS, Tag.TAG_LIST)) {
                    ListTag enchantmentTag = mekData.getList(NBTConstants.ENCHANTMENTS, Tag.TAG_COMPOUND);
                    int moduleLevel = EnchantmentHelper.deserializeEnchantments(enchantmentTag).getOrDefault(enchantment, 0);
                    if (moduleLevel > cir.getReturnValue()) {
                        cir.setReturnValue(moduleLevel);
                    }
                }
            }
        }
    }
}
package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.element.button.TranslationButton;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiModuleTweaker.class)
public abstract class MixinGuiModuleTweaker {

    @Shadow private TranslationButton optionsButton;

    @Shadow private ItemStack getStack(int index) {
        return ItemStack.EMPTY;
    }

    @Inject(method = "select", at = @At(value = "RETURN", ordinal = 0), remap = false)
    private void injectSelect(int index, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = getStack(index);
        if (!stack.isEmpty() && MekSuitGemIntegration.isMekArmor(stack)) {
            if (stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET) {
                optionsButton.active = true;
            }
        }
    }
}
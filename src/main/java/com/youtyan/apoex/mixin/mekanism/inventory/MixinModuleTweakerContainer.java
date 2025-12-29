package com.youtyan.apoex.mixin.mekanism.inventory;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModuleTweakerContainer.class)
public class MixinModuleTweakerContainer {

    @Inject(method = "isTweakableItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectIsTweakableItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MekSuitGemIntegration.isMekArmor(stack)) {
            cir.setReturnValue(true);
        }
    }
}
package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.client.render.item.MekaSuitBarDecorator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class MixinGuiGraphics {

    @Inject(
            method = {
                    "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    "m_280208_"
            },
            at = @At("TAIL")
    )
    private void injectRenderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (!stack.isEmpty() && MekSuitGemIntegration.isMekArmor(stack)) {
            MekaSuitBarDecorator.INSTANCE.render((GuiGraphics) (Object) this, font, stack, x, y);
        }
    }
}
package com.youtyan.apoex.mixin.mekanism.client;

import mekanism.client.render.item.ChemicalFluidBarDecorator;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChemicalFluidBarDecorator.class)
public interface ChemicalFluidBarDecoratorAccessor {
    @Invoker("renderBar")
    static void invokeRenderBar(GuiGraphics guiGraphics, int x, int y, long amount, long capacity, int color) {
        throw new AssertionError();
    }
}
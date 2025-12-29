package com.youtyan.apoex.client.tooltip;

import com.youtyan.apoex.client.ApoExSocketRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ExSocketTooltipComponent(ExSocketTooltipData data) implements ClientTooltipComponent {

    @Override
    public int getHeight() {
        return Minecraft.getInstance().font.lineHeight + 2;
    }

    @Override
    public int getWidth(Font font) {
        ItemStack stack = data.stack();
        return 100;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        ApoExSocketRenderHelper.renderSocket(gfx, x, y, data.stack());
        ApoExSocketRenderHelper.renderText(gfx, font, x, y, data.stack());
    }
}
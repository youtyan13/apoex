package com.youtyan.apoex.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ExSocketTooltipData(ItemStack stack) implements TooltipComponent {
}
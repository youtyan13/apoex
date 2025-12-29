package com.youtyan.apoex.item;

import net.minecraft.world.item.ItemStack;

public interface IExGem {

    default boolean canApplyTo(ItemStack targetStack) {
        return true;
    }

    default void onApplied(ItemStack targetStack) {}
}

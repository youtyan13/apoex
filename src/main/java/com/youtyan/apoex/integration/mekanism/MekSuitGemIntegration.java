package com.youtyan.apoex.integration.mekanism;

import com.youtyan.apoex.util.ApoExSocketHelper;
import net.minecraft.world.item.ItemStack;

public class MekSuitGemIntegration {
    public static final String MEK_SUITS_GEM_ID = "apoex:mek_suits_gem";

    public static boolean isMekArmor(ItemStack stack) {
        return ApoExSocketHelper.hasGem(stack, MEK_SUITS_GEM_ID);
    }
}
package com.youtyan.apoex.item.mekanism;

import com.youtyan.apoex.item.IExGem;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.config.MekanismConfig;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MekSuitsGemItem extends Item implements IExGem {

    public MekSuitsGemItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canApplyTo(ItemStack targetStack) {
        return !(targetStack.getItem() instanceof IModuleContainerItem);
    }
    
    @Override
    public void onApplied(ItemStack stack) {
        CompoundTag mekData = new CompoundTag();
        mekData.putString("energy", MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get().toString());
        mekData.put(NBTConstants.GAS_TANKS, new CompoundTag());
        mekData.put(NBTConstants.FLUID_TANKS, new CompoundTag());
        stack.getOrCreateTag().put("mekData", mekData);
    }
}
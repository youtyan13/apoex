package com.youtyan.apoex.item.mekanism;

import com.youtyan.apoex.item.IExGem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MekSuitsGemItem extends Item implements IExGem {

    public MekSuitsGemItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canApplyTo(ItemStack targetStack) {
        try {
            Class<?> moduleContainerClass = Class.forName("mekanism.common.content.gear.IModuleContainerItem");
            return !moduleContainerClass.isInstance(targetStack.getItem());
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
    
    @Override
    public void onApplied(ItemStack stack) {
        try {
            Class<?> configClass = Class.forName("mekanism.common.config.MekanismConfig");
            Object gearConfig = configClass.getField("gear").get(null);
            Object energyCapacityConfig = gearConfig.getClass().getField("mekaSuitBaseEnergyCapacity").get(gearConfig);
            Object energyCapacity = energyCapacityConfig.getClass().getMethod("get").invoke(energyCapacityConfig);
            
            Class<?> nbtConstantsClass = Class.forName("mekanism.api.NBTConstants");
            String gasTanksKey = (String) nbtConstantsClass.getField("GAS_TANKS").get(null);
            String fluidTanksKey = (String) nbtConstantsClass.getField("FLUID_TANKS").get(null);

            CompoundTag mekData = new CompoundTag();
            mekData.putString("energy", energyCapacity.toString());
            mekData.put(gasTanksKey, new CompoundTag());
            mekData.put(fluidTanksKey, new CompoundTag());
            stack.getOrCreateTag().put("mekData", mekData);
        } catch (Exception e) {
            System.err.println("Failed to apply Mekanism data to item: " + e.getMessage());
        }
    }
}
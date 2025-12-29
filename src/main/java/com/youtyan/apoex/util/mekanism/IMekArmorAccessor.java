package com.youtyan.apoex.util.mekanism;

import mekanism.api.chemical.gas.Gas;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;

import java.util.List;

public interface IMekArmorAccessor {
    List<ChemicalTankSpec<Gas>> apoex$getGasTankSpecs();
    List<FluidTankSpec> apoex$getFluidTankSpecs();
}
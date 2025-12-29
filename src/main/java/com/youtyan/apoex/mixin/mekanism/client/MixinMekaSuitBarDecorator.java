package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.IMekArmorAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.client.render.item.MekaSuitBarDecorator;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;
import mekanism.common.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(MekaSuitBarDecorator.class)
public abstract class MixinMekaSuitBarDecorator {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && MekSuitGemIntegration.isMekArmor(stack) && stack.getItem() instanceof IMekArmorAccessor armor) {

            yOffset += 12;

            if (apoex$tryRender(guiGraphics, stack, Capabilities.GAS_HANDLER, xOffset, yOffset, armor.apoex$getGasTankSpecs())) {
                yOffset--;
            }

            List<FluidTankSpec> fluidTankSpecs = armor.apoex$getFluidTankSpecs();
            if (!fluidTankSpecs.isEmpty()) {
                Optional<IFluidHandlerItem> capabilityInstance = FluidUtil.getFluidHandler(stack).resolve();
                if (capabilityInstance.isPresent()) {
                    IFluidHandlerItem fluidHandler = capabilityInstance.get();
                    int tank = apoex$getDisplayTank(fluidTankSpecs, stack, fluidHandler.getTanks());
                    if (tank != -1) {
                        FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                        ChemicalFluidBarDecoratorAccessor.invokeRenderBar(guiGraphics, xOffset, yOffset, fluidInTank.getAmount(), fluidHandler.getTankCapacity(tank),
                                FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0xFFFFFFFF));
                    }
                }
            }

            cir.setReturnValue(true);
        }
    }

    @Unique private <CHEMICAL extends Chemical<CHEMICAL>> boolean apoex$tryRender(GuiGraphics guiGraphics, ItemStack stack, Capability<? extends IChemicalHandler<CHEMICAL, ?>> capability, int xOffset, int yOffset, List<ChemicalTankSpec<CHEMICAL>> chemicalTankSpecs) { if (!chemicalTankSpecs.isEmpty() && chemicalTankSpecs.stream().anyMatch(spec -> spec.supportsStack(stack))) { Optional<? extends IChemicalHandler<CHEMICAL, ?>> capabilityInstance = stack.getCapability(capability).resolve(); if (capabilityInstance.isPresent()) { IChemicalHandler<CHEMICAL, ?> chemicalHandler = capabilityInstance.get(); int tank = apoex$getDisplayTank(chemicalTankSpecs, stack, chemicalHandler.getTanks()); if (tank != -1) { ChemicalStack<CHEMICAL> chemicalInTank = chemicalHandler.getChemicalInTank(tank); ChemicalFluidBarDecoratorAccessor.invokeRenderBar(guiGraphics, xOffset, yOffset, chemicalInTank.getAmount(), chemicalHandler.getTankCapacity(tank), chemicalInTank.getChemicalColorRepresentation()); return true; } } } return false; }
    @Unique private static <TYPE> int apoex$getDisplayTank(List<? extends GenericTankSpec<TYPE>> tankSpecs, ItemStack stack, int tanks) { if (tanks == 0) return -1; if (tanks > 1 && tanks == tankSpecs.size() && Minecraft.getInstance().level != null) { IntList tankIndices = new IntArrayList(tanks); for (int i = 0; i < tanks; i++) { if (tankSpecs.get(i).supportsStack(stack)) tankIndices.add(i); } if (tankIndices.isEmpty()) return -1; else if (tankIndices.size() == 1) return tankIndices.getInt(0); return tankIndices.getInt((int) (Minecraft.getInstance().level.getGameTime() / 20) % tankIndices.size()); } for (int i = 0; i < tanks && i < tankSpecs.size(); i++) { if (tankSpecs.get(i).supportsStack(stack)) return i; } return -1; }
}
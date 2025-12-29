package com.youtyan.apoex.mixin.mekanism.module;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleNutritionalInjectionUnit;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ModuleNutritionalInjectionUnit.class)
public abstract class MixinModuleNutritionalInjectionUnit implements ICustomModule<ModuleNutritionalInjectionUnit> {

    @Unique
    private static final ResourceLocation FALLBACK_ICON = MekanismUtils.getResource(ResourceType.GUI_HUD, "nutritional_injection_unit.png");

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectTickServer(IModule<ModuleNutritionalInjectionUnit> module, Player player, CallbackInfo ci) {
        ItemStack container = module.getContainer();
        if (MekSuitGemIntegration.isMekArmor(container)) {
            ci.cancel();

            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
            if (MekanismUtils.isPlayingMode(player) && player.canEat(false)) {
                Optional<IFluidHandlerItem> fluidHandlerCap = container.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
                if (fluidHandlerCap.isPresent()) {
                    IFluidHandlerItem fluidHandler = fluidHandlerCap.get();
                    int pasteMBPerFood = MekanismConfig.general.nutritionalPasteMBPerFood.get();

                    int containedPaste = 0;
                    for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                        FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                        if (fluidInTank.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid()) {
                            containedPaste += fluidInTank.getAmount();
                        }
                    }
                    int canProvide = containedPaste / pasteMBPerFood;

                    if (canProvide > 0) {
                        int needed = 20 - player.getFoodData().getFoodLevel();

                        int toFeed = Math.min(canProvide, needed);

                        int energyAffordable = module.getContainerEnergy().divideToInt(usage);
                        toFeed = Math.min(toFeed, energyAffordable);

                        if (toFeed > 0) {
                            module.useEnergy(player, usage.multiply(toFeed));
                            fluidHandler.drain(MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(toFeed * pasteMBPerFood), IFluidHandler.FluidAction.EXECUTE);
                            player.getFoodData().eat(toFeed, MekanismConfig.general.nutritionalPasteSaturation.get());
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "addHUDElements", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectAddHUDElements(IModule<ModuleNutritionalInjectionUnit> module, Player player, Consumer<IHUDElement> hudElementAdder, CallbackInfo ci) {
        ItemStack container = module.getContainer();
        if (MekSuitGemIntegration.isMekArmor(container)) {
            ci.cancel();
            if (module.isEnabled()) {
                long capacity = MekanismConfig.gear.mekaSuitNutritionalMaxStorage.get();
                long stored = 0;
                var cap = container.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                if (cap.isPresent()) {
                    var handler = cap.resolve().get();
                    for (int i = 0; i < handler.getTanks(); i++) {
                        if (handler.getFluidInTank(i).getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid()) {
                            stored += handler.getFluidInTank(i).getAmount();
                        }
                    }
                }

                double ratio = StorageUtils.getRatio(stored, capacity);
                hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(FALLBACK_ICON, ratio));
            }
        }
    }
}
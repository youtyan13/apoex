package com.youtyan.apoex.mixin.mekanism;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "mekanism.common.tile.TileEntityModificationStation", remap = false)
public abstract class MixinTileEntityModificationStation extends TileEntityMekanism {

    @Shadow public InputInventorySlot containerSlot;
    @Shadow public InputInventorySlot moduleSlot;
    @Shadow public EnergyInventorySlot energySlot;
    @Shadow private MachineEnergyContainer<TileEntityModificationStation> energyContainer;
    @Shadow public int ticksRequired;
    @Shadow public int operatingTicks;
    @Shadow private boolean usedEnergy;

    public MixinTileEntityModificationStation(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    @Redirect(
            method = "getInitialInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/inventory/slot/InputInventorySlot;at(Ljava/util/function/Predicate;Lmekanism/api/IContentsListener;II)Lmekanism/common/inventory/slot/InputInventorySlot;",
                    ordinal = 1
            )
    )
    private InputInventorySlot redirectContainerSlot(java.util.function.Predicate<ItemStack> predicate, mekanism.api.IContentsListener listener, int x, int y) {
        return InputInventorySlot.at(stack -> predicate.test(stack) || MekSuitGemIntegration.isMekArmor(stack), listener, x, y);
    }

    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    protected void injectOnUpdateServer(CallbackInfo ci) {
        if (!containerSlot.isEmpty() && MekSuitGemIntegration.isMekArmor(containerSlot.getStack())) {
            energySlot.fillContainerOrConvert();
            FloatingLong clientEnergyUsed = FloatingLong.ZERO;

            if (MekanismUtils.canFunction(this)) {
                boolean operated = false;
                if (energyContainer.getEnergy().greaterOrEqual(energyContainer.getEnergyPerTick()) && !moduleSlot.isEmpty()) {
                    ModuleData<?> data = ((mekanism.common.content.gear.IModuleItem) moduleSlot.getStack().getItem()).getModuleData();
                    ItemStack stack = containerSlot.getStack();

                    if (IModuleHelper.INSTANCE.getSupported(stack).contains(data)) {
                        IModule<?> module = IModuleHelper.INSTANCE.load(stack, data);
                        if (module == null || module.getInstalledCount() < data.getMaxStackSize()) {
                            operated = true;
                            operatingTicks++;
                            clientEnergyUsed = energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);

                            if (operatingTicks >= ticksRequired) {
                                operatingTicks = 0;

                                ItemStack newStack = stack.copy();
                                ApoExModuleHelper.addModule(newStack, data);

                                containerSlot.setStack(newStack);
                                setChanged();
                                MekanismUtils.logMismatchedStackSize(moduleSlot.shrinkStack(1, Action.EXECUTE), 1);
                                setChanged();
                            }
                        }
                    }
                }
                if (!operated) {
                    operatingTicks = 0;
                }
            }
            usedEnergy = !clientEnergyUsed.isZero();

            ci.cancel();
        }
    }

    @Inject(method = "removeModule", at = @At("HEAD"), cancellable = true)
    public void injectRemoveModule(Player player, ModuleData<?> type, CallbackInfo ci) {
        ItemStack stack = containerSlot.getStack();
        if (!stack.isEmpty() && MekSuitGemIntegration.isMekArmor(stack)) {
            ItemStack newStack = stack.copy();
            if (ApoExModuleHelper.hasModule(newStack, type) && player.getInventory().add(type.getItemProvider().getItemStack())) {
                ApoExModuleHelper.removeModule(newStack, type);
                containerSlot.setStack(newStack);
                setChanged();
            }
            ci.cancel();
        }
    }
}
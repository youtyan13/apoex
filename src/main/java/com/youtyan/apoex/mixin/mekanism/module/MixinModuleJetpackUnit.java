package com.youtyan.apoex.mixin.mekanism.module;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ModuleJetpackUnit.class)
public abstract class MixinModuleJetpackUnit implements ICustomModule<ModuleJetpackUnit> {

    @Shadow(remap = false)
    private IModuleConfigItem<JetpackMode> jetpackMode;

    @Inject(method = "addHUDElements", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectAddHUDElements(IModule<ModuleJetpackUnit> module, Player player, Consumer<IHUDElement> hudElementAdder, CallbackInfo ci) {
        ItemStack container = module.getContainer();
        if (MekSuitGemIntegration.isMekArmor(container)) {
            ci.cancel();
            if (module.isEnabled()) {
                long capacity = MekanismConfig.gear.mekaSuitJetpackMaxStorage.get();
                long stored = 0;
                var cap = container.getCapability(Capabilities.GAS_HANDLER);
                if (cap.isPresent()) {
                    var handler = cap.resolve().get();
                    for (int i = 0; i < handler.getTanks(); i++) {
                        if (handler.getChemicalInTank(i).getType() == MekanismGases.HYDROGEN.get()) {
                            stored += handler.getChemicalInTank(i).getAmount();
                        }
                    }
                }

                double ratio = StorageUtils.getRatio(stored, capacity);
                hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(jetpackMode.get().getHUDIcon(), ratio));
            }
        }
    }
}
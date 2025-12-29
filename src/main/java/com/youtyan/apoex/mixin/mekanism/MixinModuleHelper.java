package com.youtyan.apoex.mixin.mekanism;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.content.gear.Module;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(targets = "mekanism.common.content.gear.ModuleHelper", remap = false)
public abstract class MixinModuleHelper {

    @Inject(method = "getSupported(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Set;", at = @At("RETURN"), cancellable = true)
    private void injectGetSupported(ItemStack stack, CallbackInfoReturnable<Set<ModuleData<?>>> cir) {
        if (MekSuitGemIntegration.isMekArmor(stack) && stack.getItem() instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getType().getSlot();
            Set<ModuleData<?>> supported = new HashSet<>(cir.getReturnValue());

            IItemProvider mekaSuitProvider = switch (slot) {
                case HEAD -> MekanismItems.MEKASUIT_HELMET;
                case CHEST -> MekanismItems.MEKASUIT_BODYARMOR;
                case LEGS -> MekanismItems.MEKASUIT_PANTS;
                case FEET -> MekanismItems.MEKASUIT_BOOTS;
                default -> null;
            };

            if (mekaSuitProvider != null) {
                supported.addAll(IModuleHelper.INSTANCE.getSupported(new ItemStack(mekaSuitProvider)));
            }

            Collection<IModule<?>> installed = ApoExModuleHelper.getModules(stack);
            Set<ModuleData<?>> toRemove = new HashSet<>();

            for (ModuleData<?> candidate : supported) {
                for (IModule<?> module : installed) {
                    if (module.getData() == candidate) continue;
                    if (candidate.isExclusive(module.getData().getExclusiveFlags())) {
                        toRemove.add(candidate);
                        break;
                    }
                }
            }
            supported.removeAll(toRemove);

            cir.setReturnValue(supported);
        }
    }

    @Inject(method = "load(Lnet/minecraft/world/item/ItemStack;Lmekanism/api/providers/IModuleDataProvider;)Lmekanism/common/content/gear/Module;", at = @At("HEAD"), cancellable = true)
    private <MODULE extends ICustomModule<MODULE>> void injectLoad(ItemStack container, IModuleDataProvider<MODULE> typeProvider, CallbackInfoReturnable<Module<MODULE>> cir) {
        if (MekSuitGemIntegration.isMekArmor(container)) {
            IModule<MODULE> module = ApoExModuleHelper.getModule(container, typeProvider.getModuleData());
            if (module instanceof Module) {
                cir.setReturnValue((Module<MODULE>) module);
            }
        }
    }

    @Inject(method = "loadAll(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void injectLoadAll(ItemStack container, CallbackInfoReturnable<List<Module<?>>> cir) {
        if (MekSuitGemIntegration.isMekArmor(container)) {
            List<Module<?>> modules = new ArrayList<>();
            for (IModule<?> module : ApoExModuleHelper.getModules(container)) {
                if (module instanceof Module) {
                    modules.add((Module<?>) module);
                }
            }
            cir.setReturnValue(modules);
        }
    }

    @Inject(method = "loadAll(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Class;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private <MODULE extends ICustomModule<?>> void injectLoadAllClass(ItemStack container, Class<MODULE> moduleClass, CallbackInfoReturnable<List<Module<? extends MODULE>>> cir) {
        if (MekSuitGemIntegration.isMekArmor(container)) {
            List<Module<? extends MODULE>> modules = new ArrayList<>();
            for (IModule<?> module : ApoExModuleHelper.getModules(container)) {
                if (module instanceof Module && moduleClass.isInstance(module.getCustomInstance())) {
                    modules.add((Module<? extends MODULE>) module);
                }
            }
            cir.setReturnValue(modules);
        }
    }
}
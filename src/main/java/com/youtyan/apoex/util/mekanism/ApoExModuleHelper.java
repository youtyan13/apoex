package com.youtyan.apoex.util.mekanism;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.Module;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ApoExModuleHelper {

    @Nullable
    private static CompoundTag getMekDataTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getCompound("mekData") : null;
    }

    private static CompoundTag getOrCreateMekDataTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("mekData")) {
            tag.put("mekData", new CompoundTag());
        }
        return tag.getCompound("mekData");
    }

    @Nullable
    private static CompoundTag getModulesTag(ItemStack stack) {
        CompoundTag mekData = getMekDataTag(stack);
        return mekData != null ? mekData.getCompound("modules") : null;
    }

    private static CompoundTag getOrCreateModulesTag(ItemStack stack) {
        CompoundTag mekData = getOrCreateMekDataTag(stack);
        if (!mekData.contains("modules")) {
            mekData.put("modules", new CompoundTag());
        }
        return mekData.getCompound("modules");
    }

    @Nullable
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ItemStack stack, ModuleData<MODULE> type) {
        CompoundTag modules = getModulesTag(stack);
        if (modules != null && modules.contains(type.getRegistryName().toString())) {
            Module<MODULE> module = new Module<>(type, stack);
            module.read(modules.getCompound(type.getRegistryName().toString()));
            return module;
        }
        return null;
    }

    public static boolean hasModule(ItemStack stack, ModuleData<?> type) {
        CompoundTag modules = getModulesTag(stack);
        return modules != null && modules.contains(type.getRegistryName().toString());
    }

    public static Collection<IModule<?>> getModules(ItemStack stack) {
        CompoundTag modulesTag = getModulesTag(stack);
        if (modulesTag == null) return Collections.emptyList();

        List<IModule<?>> result = new ArrayList<>();
        IForgeRegistry<ModuleData<?>> registry = MekanismAPI.moduleRegistry();
        for (String key : modulesTag.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(key);
            if (rl != null) {
                ModuleData<?> data = registry.getValue(rl);
                if (data != null) {
                    addModuleToList(result, stack, data);
                }
            }
        }
        return result;
    }

    private static <T extends ICustomModule<T>> void addModuleToList(List<IModule<?>> list, ItemStack stack, ModuleData<T> data) {
        IModule<T> module = getModule(stack, data);
        if (module != null) {
            list.add(module);
        }
    }

    public static <T extends ICustomModule<T>> void addModule(ItemStack stack, ModuleData<T> type) {
        CompoundTag modules = getOrCreateModulesTag(stack);
        String typeName = type.getRegistryName().toString();
        boolean first = !modules.contains(typeName);
        if (!modules.contains(typeName)) {
            CompoundTag moduleTag = new CompoundTag();
            moduleTag.putBoolean("enabled", true);
            moduleTag.putInt("amount", 1);
            modules.put(typeName, moduleTag);
        } else {
            incrementModule(stack, type);
        }
        IModule<T> module = getModule(stack, type);
        if (module != null) {
            module.getCustomInstance().onAdded(module, first);
        }
    }

    private static <T extends ICustomModule<T>> void incrementModule(ItemStack stack, ModuleData<T> type) {
        IModule<T> module = getModule(stack, type);
        if (module != null && module.getInstalledCount() < type.getMaxStackSize()) {
            setModuleInstalledCount(stack, module, module.getInstalledCount() + 1);
        }
    }

    public static void removeModule(ItemStack stack, ModuleData<?> type) {
        CompoundTag modules = getModulesTag(stack);
        if (modules != null) {
            decrementModule(stack, type, modules);
        }
    }

    private static <T extends ICustomModule<T>> void decrementModule(ItemStack stack, ModuleData<T> type, CompoundTag modules) {
        IModule<T> module = getModule(stack, type);
        if (module != null) {
            if (module.getInstalledCount() > 1) {
                setModuleInstalledCount(stack, module, module.getInstalledCount() - 1);
            } else {
                modules.remove(type.getRegistryName().toString());
            }
        }
    }

    private static void setModuleInstalledCount(ItemStack stack, IModule<?> module, int count) {
        CompoundTag modules = getOrCreateModulesTag(stack);
        CompoundTag moduleTag = modules.getCompound(module.getData().getRegistryName().toString());
        moduleTag.putInt("amount", count);
    }
}

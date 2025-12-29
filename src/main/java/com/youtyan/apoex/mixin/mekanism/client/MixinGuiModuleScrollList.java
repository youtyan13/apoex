package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.scroll.GuiScrollList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = GuiModuleScrollList.class, remap = false)
public abstract class MixinGuiModuleScrollList extends GuiScrollList {

    public MixinGuiModuleScrollList(mekanism.client.gui.IGuiWrapper gui, int x, int y, int width, int height, int itemHeight, net.minecraft.resources.ResourceLocation background, int xStart) {
        super(gui, x, y, width, height, itemHeight, background, xStart);
    }

    @Redirect(
            method = "updateItemAndList",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/api/gear/IModuleHelper;loadAllTypes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private List<ModuleData<?>> redirectLoadAllTypes(IModuleHelper instance, ItemStack stack) {
        if (MekSuitGemIntegration.isMekArmor(stack)) {
            return ApoExModuleHelper.getModules(stack).stream()
                    .map(IModule::getData)
                    .collect(Collectors.toList());
        }
        return instance.loadAllTypes(stack);
    }
}
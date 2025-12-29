package com.youtyan.apoex.mixin.mekanism.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.render.HUDRenderer;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(HUDRenderer.class)
public abstract class MixinHUDRenderer {

    @Shadow @Final private static EquipmentSlot[] EQUIPMENT_ORDER;

    @Shadow
    private void renderHUDElement(Font font, GuiGraphics guiGraphics, int x, int y, IHUDElement element, int color, boolean iconRight) {
        throw new AbstractMethodError("Shadowed method");
    }

    @Redirect(method = "renderMekaSuitEnergyIcons", at = @At(value = "INVOKE", target = "Lmekanism/client/render/HUDRenderer;renderEnergyIcon(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/Predicate;)I"), remap = false)
    private int redirectRenderEnergyIcon(HUDRenderer instance, Player player, Font font, GuiGraphics guiGraphics, int posX, int color, ResourceLocation icon, EquipmentSlot slot, Predicate<Item> showPercent) {
        ItemStack stack = player.getItemBySlot(slot);
        if (showPercent.test(stack.getItem()) || MekSuitGemIntegration.isMekArmor(stack)) {
            renderHUDElement(font, guiGraphics, posX, 0, IModuleHelper.INSTANCE.hudElementPercent(icon, StorageUtils.getEnergyRatio(stack)), color, false);
            return 48;
        }
        return 0;
    }

    @Inject(method = "renderMekaSuitModuleIcons", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectRenderMekaSuitModuleIcons(Player player, Font font, GuiGraphics guiGraphics, int screenWidth, int screenHeight, boolean reverseHud, int color, CallbackInfo ci) {
        ci.cancel();

        int startX = screenWidth - 10;
        int curY = screenHeight - 10;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        for (EquipmentSlot type : EQUIPMENT_ORDER) {
            ItemStack stack = player.getItemBySlot(type);
            List<IHUDElement> hudElements = new ArrayList<>();

            if (stack.getItem() instanceof IModuleContainerItem item) {
                hudElements.addAll(item.getHUDElements(player, stack));
            } else if (MekSuitGemIntegration.isMekArmor(stack)) {
                for (IModule<?> module : ApoExModuleHelper.getModules(stack)) {
                    if (module.renderHUD()) {
                        apoex$addHUDElements(module, player, hudElements);
                    }
                }
            }

            for (IHUDElement element : hudElements) {
                curY -= 18;
                if (reverseHud) {
                    renderHUDElement(font, guiGraphics, 10, curY, element, color, false);
                } else {
                    int elementWidth = 24 + font.width(element.getText());
                    renderHUDElement(font, guiGraphics, startX - elementWidth, curY, element, color, true);
                }
            }
        }
        pose.popPose();
    }

    @Unique
    private <MODULE extends ICustomModule<MODULE>> void apoex$addHUDElements(IModule<MODULE> module, Player player, List<IHUDElement> hudElements) {
        module.getCustomInstance().addHUDElements(module, player, hudElements::add);
    }
}
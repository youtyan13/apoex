package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.render.hud.MekaSuitEnergyLevel;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MekaSuitEnergyLevel.class)
public class MixinMekaSuitEnergyLevel {

    @Unique
    private static final ResourceLocation POWER_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power_long.png");

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            FloatingLong capacity = FloatingLong.ZERO;
            FloatingLong stored = FloatingLong.ZERO;

            if (gui.getMinecraft().player != null) {
                for (ItemStack stack : gui.getMinecraft().player.getArmorSlots()) {
                    if (stack.getItem() instanceof ItemMekaSuitArmor || MekSuitGemIntegration.isMekArmor(stack)) {
                        IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
                        if (container != null) {
                            capacity = capacity.plusEqual(container.getMaxEnergy());
                            stored = stored.plusEqual(container.getEnergy());
                        }
                    }
                }
            }

            if (!capacity.isZero()) {
                int x = screenWidth / 2 - 91;
                int y = screenHeight - gui.leftHeight + 2;
                int length = (int)Math.round(stored.divide(capacity).doubleValue() * 79.0D);
                GuiUtils.renderExtendedTexture(guiGraphics, GuiBar.BAR, 2, 2, x, y, 81, 6);
                guiGraphics.blit(POWER_BAR, x + 1, y + 1, length, 4, 0.0F, 0.0F, length, 4, 79, 4);
                gui.leftHeight += 8;
            }
        }
        ci.cancel();
    }
}
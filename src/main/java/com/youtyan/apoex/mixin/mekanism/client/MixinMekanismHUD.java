package com.youtyan.apoex.mixin.mekanism.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.HUDRenderer;
import mekanism.client.render.hud.MekanismHUD;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(MekanismHUD.class)
public abstract class MixinMekanismHUD {

    @Shadow @Final private HUDRenderer hudRenderer;
    @Shadow @Final private static EquipmentSlot[] EQUIPMENT_ORDER;

    @Shadow
    private int makeComponent(Consumer<List<Component>> adder, List<List<Component>> initial) {
        throw new AbstractMethodError("Shadowed method");
    }

    @Overwrite(remap = false)
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        Minecraft minecraft = gui.getMinecraft();
        Player player = minecraft.player;
        if (!minecraft.options.hideGui && player != null && !player.isSpectator() && MekanismConfig.client.enableHUD.get()) {
            int count = 0;
            List<List<Component>> renderStrings = new ArrayList<>();
            for (EquipmentSlot slotType : EQUIPMENT_ORDER) {
                ItemStack stack = player.getItemBySlot(slotType);
                if (stack.getItem() instanceof IItemHUDProvider hudProvider) {
                    count += makeComponent(list -> hudProvider.addHUDStrings(list, player, stack, slotType), renderStrings);
                }
            }
            if (Mekanism.hooks.CuriosLoaded) {
                Optional<? extends IItemHandler> invOptional = CuriosIntegration.getCuriosInventory(player);
                if (invOptional.isPresent()) {
                    IItemHandler inv = invOptional.get();
                    for (int i = 0, slots = inv.getSlots(); i < slots; i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof IItemHUDProvider hudProvider) {
                            count += makeComponent(list -> hudProvider.addCurioHUDStrings(list, player, stack), renderStrings);
                        }
                    }
                }
            }
            Font font = gui.getFont();
            boolean reverseHud = MekanismConfig.client.reverseHUD.get();
            int maxTextHeight = screenHeight;
            if (count > 0) {
                float hudScale = MekanismConfig.client.hudScale.get();
                int xScale = (int) (screenWidth / hudScale);
                int yScale = (int) (screenHeight / hudScale);
                int start = (renderStrings.size() * 2) + (count * 9);
                int y = yScale - start;
                maxTextHeight = (int) (y * hudScale);
                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                pose.scale(hudScale, hudScale, hudScale);

                int backgroundColor = minecraft.options.getBackgroundColor(0.0F);
                if (backgroundColor != 0) {
                    int maxTextWidth = 0;
                    for (List<Component> group : renderStrings) {
                        for (Component text : group) {
                            int textWidth = font.width(text);
                            if (textWidth > maxTextWidth) {
                                maxTextWidth = textWidth;
                            }
                        }
                    }
                    int x = reverseHud ? xScale - maxTextWidth - 2 : 2;
                    GuiUtils.drawBackdrop(guiGraphics, Minecraft.getInstance(), x, y, maxTextWidth, maxTextHeight, 0xFFFFFFFF);
                }

                for (List<Component> group : renderStrings) {
                    for (Component text : group) {
                        int textWidth = font.width(text);
                        int x = reverseHud ? xScale - textWidth - 2 : 2;
                        guiGraphics.drawString(font, text, x, y, 0xFFC8C8C8);
                        y += 9;
                    }
                    y += 2;
                }
                pose.popPose();
            }

            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (headStack.is(MekanismTags.Items.MEKASUIT_HUD_RENDERER) || MekSuitGemIntegration.isMekArmor(headStack)) {
                hudRenderer.renderHUD(minecraft, guiGraphics, font, partialTicks, screenWidth, screenHeight, maxTextHeight, reverseHud);
            }
        }
    }
}
package com.youtyan.apoex.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.youtyan.apoex.util.ApoExSocketHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ApoExSocketRenderHelper {

    public static final ResourceLocation SPECIAL_SOCKET_TEXTURE = new ResourceLocation("apoex", "textures/gui/special_socket.png");

    private static final float SOCKET_COLOR_R = 1.0F;
    private static final float SOCKET_COLOR_G = 0.8F;
    private static final float SOCKET_COLOR_B = 0.0F;
    private static final ChatFormatting TEXT_COLOR = ChatFormatting.GOLD;

    public static void renderSocket(GuiGraphics gfx, int x, int y, ItemStack stack) {
        if (!ApoExSocketHelper.hasSocket(stack)) return;

        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(0, 0, 400.0F);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(SOCKET_COLOR_R, SOCKET_COLOR_G, SOCKET_COLOR_B, 1.0F);

        gfx.blit(SPECIAL_SOCKET_TEXTURE, x, y, 0, 0, 9, 9, 9, 9);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        if (ApoExSocketHelper.hasGem(stack)) {
            ItemStack gemStack = ApoExSocketHelper.getGem(stack);
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 1.0F);
            gfx.renderFakeItem(gemStack, 2 * x + 1, 2 * y + 1);
            pose.popPose();
        }
        pose.popPose();
    }

    public static void renderText(GuiGraphics gfx, Font font, int x, int y, ItemStack stack) {
        if (!ApoExSocketHelper.hasSocket(stack)) return;

        Component text;
        if (ApoExSocketHelper.hasGem(stack)) {
            text = ApoExSocketHelper.getGem(stack).getHoverName().copy().withStyle(ChatFormatting.YELLOW);
        } else {
            text = Component.translatable("socket.apoex.empty").withStyle(TEXT_COLOR);
            if (text.getString().equals("socket.apoex.empty")) {
                text = Component.literal("Empty Special Socket").withStyle(TEXT_COLOR);
            }
        }

        gfx.drawString(font, text, x + 12, y + 1, 0xFFFFFFFF, true);
    }
}
package com.youtyan.apoex.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.youtyan.apoex.client.ApoExSocketRenderHelper;
import com.youtyan.apoex.util.ApoExSocketHelper;
import dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SocketTooltipRenderer.class)
public abstract class MixinSocketTooltipRenderer {

    @Shadow(remap = false)
    private SocketTooltipRenderer.SocketComponent comp;

    @Shadow(remap = false)
    private int spacing;

    @Shadow(remap = false)
    public static final ResourceLocation SOCKET = new ResourceLocation("apotheosis", "textures/gui/socket.png");

    @Shadow(remap = false)
    public static Component getSocketDesc(GemInstance gem) {
        throw new AbstractMethodError("Shadow");
    }

    @Inject(method = {"getHeight", "m_142103_"}, at = @At("RETURN"), cancellable = true, remap = false)
    public void apoex$addHeight(CallbackInfoReturnable<Integer> cir) {
        if (this.comp != null && ApoExSocketHelper.hasSocket(this.comp.socketed())) {
            cir.setReturnValue(cir.getReturnValue() + this.spacing);
        }
    }

    @Overwrite(remap = false)
    public void m_183452_(Font font, int x, int y, GuiGraphics gfx) {
        for (int i = 0; i < this.comp.gems().size(); ++i) {
            gfx.blit(SOCKET, x, y + this.spacing * i, 0, 0.0F, 0.0F, 9, 9, 9, 9);
        }

        int originalY = y;
        for (GemInstance inst : this.comp.gems()) {
            if (inst.isValid()) {
                PoseStack pose = gfx.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1.0F);
                gfx.renderFakeItem(inst.gemStack(), 2 * x + 1, 2 * originalY + 1);
                pose.popPose();
            }
            originalY += this.spacing;
        }

        if (this.comp != null && ApoExSocketHelper.hasSocket(this.comp.socketed())) {
            ApoExSocketRenderHelper.renderSocket(gfx, x, originalY, this.comp.socketed());
        }
    }

    @Overwrite(remap = false)
    public void m_142440_(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
        for (int i = 0; i < this.comp.gems().size(); ++i) {
            pFont.drawInBatch(getSocketDesc(this.comp.gems().get(i)), (float)(pX + 12), (float)(pY + this.spacing * i + 2), 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }

        if (this.comp != null && ApoExSocketHelper.hasSocket(this.comp.socketed())) {
            int yOffset = this.comp.gems().size() * this.spacing;
            int specialY = pY + yOffset;

            Component text;
            if (ApoExSocketHelper.hasGem(this.comp.socketed())) {
                text = ApoExSocketHelper.getGem(this.comp.socketed()).getHoverName().copy().withStyle(ChatFormatting.YELLOW);
            } else {
                text = Component.translatable("socket.apoex.empty").withStyle(ChatFormatting.GOLD);
                if (text.getString().equals("socket.apoex.empty")) {
                    text = Component.literal("Empty Special Socket").withStyle(ChatFormatting.GOLD);
                }
            }

            pFont.drawInBatch(text, (float)(pX + 12), (float)(specialY + 2), 0xFFFFFFFF, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }
}
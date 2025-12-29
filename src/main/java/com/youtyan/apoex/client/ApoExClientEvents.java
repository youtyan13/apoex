package com.youtyan.apoex.client;

import com.mojang.datafixers.util.Either;
import com.youtyan.apoex.client.tooltip.ExSocketTooltipComponent;
import com.youtyan.apoex.client.tooltip.ExSocketTooltipData;
import com.youtyan.apoex.util.ApoExSocketHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.ListIterator;

@Mod.EventBusSubscriber(modid = "apoex", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApoExClientEvents {

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ExSocketTooltipData.class, ExSocketTooltipComponent::new);
    }

    @Mod.EventBusSubscriber(modid = "apoex", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;

            if (ApoExSocketHelper.hasSocket(stack)) {
                boolean hasApotheosisSockets = false;

                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("apotheosis")) {
                    CompoundTag apoTag = tag.getCompound("apotheosis");
                    if (apoTag.contains("sockets") && apoTag.getInt("sockets") > 0) {
                        hasApotheosisSockets = true;
                    }
                }

                if (!hasApotheosisSockets) {
                    hasApotheosisSockets = event.getTooltipElements().stream()
                            .map(e -> e.right())
                            .filter(java.util.Optional::isPresent)
                            .map(java.util.Optional::get)
                            .anyMatch(c -> c.getClass().getName().contains("Socket"));
                }

                if (!hasApotheosisSockets) {
                    List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
                    int addIndex = elements.size();

                    ListIterator<Either<FormattedText, TooltipComponent>> iterator = elements.listIterator(elements.size());
                    while (iterator.hasPrevious()) {
                        Either<FormattedText, TooltipComponent> element = iterator.previous();
                        if (element.left().isPresent()) {
                            FormattedText text = element.left().get();
                            if (text instanceof Component comp) {
                                Style style = comp.getStyle();
                                boolean isAdvanced = style.getColor() != null && style.getColor().equals(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY));
                                boolean isModName = style.getColor() != null && style.getColor().equals(TextColor.fromLegacyFormat(ChatFormatting.BLUE)) && style.isItalic();

                                if (isAdvanced || isModName) {
                                    addIndex--;
                                    continue;
                                }
                            }
                        }
                        break;
                    }

                    elements.add(addIndex, Either.right(new ExSocketTooltipData(stack)));
                }
            }
        }
    }
}
package com.youtyan.apoex.mixin.mekanism.client;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ArmorItem.class)
public abstract class MixinArmorItemClient extends Item {

    @Shadow public abstract ArmorItem.Type getType();

    public MixinArmorItemClient(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekSuitGemIntegration.isMekArmor(stack)) {
            if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
                for (IModule<?> module : ApoExModuleHelper.getModules(stack)) {
                    ModuleData<?> data = module.getData();
                    int installed = module.getInstalledCount();
                    int max = data.getMaxStackSize();
                    Component name = data.getTextComponent().copy().withStyle(Style.EMPTY.withColor(EnumColor.GRAY.getColor()));
                    if (installed >= 2) {
                        Component countInfo = Component.literal(" (" + installed + "/" + max + ")").withStyle(Style.EMPTY.withColor(EnumColor.GRAY.getColor()));
                        tooltip.add(Component.empty().append(name).append(countInfo));
                    } else { tooltip.add(name); }
                }
            } else {
                StorageUtils.addStoredEnergy(stack, tooltip, true);
                if (this.getType() == ArmorItem.Type.CHESTPLATE) StorageUtils.addStoredGas(stack, tooltip, true, false);
                if (this.getType() == ArmorItem.Type.HELMET) StorageUtils.addStoredFluid(stack, tooltip, true);
                tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
        } else {
            super.appendHoverText(stack, world, tooltip, flag);
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        if (MekSuitGemIntegration.isMekArmor(stack)) return true;
        return super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        if (MekSuitGemIntegration.isMekArmor(stack)) return StorageUtils.getEnergyBarWidth(stack);
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        if (MekSuitGemIntegration.isMekArmor(stack)) return MekanismConfig.client.energyColor.get();
        return super.getBarColor(stack);
    }
}

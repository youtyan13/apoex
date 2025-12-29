package com.youtyan.apoex.compat.mekanism;

import com.youtyan.apoex.compat.ICreativeTabListener;
import com.youtyan.apoex.event.mekanism.MekSuitGemDamageHandler;
import com.youtyan.apoex.item.ApoExItems;
import com.youtyan.apoex.item.mekanism.MekSuitsGemItem;
import moffy.addonapi.AddonModule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

public class ApoExMekanismCompatModule extends AddonModule implements ICreativeTabListener {

    public static RegistryObject<Item> MEK_SUITS_GEM;

    public ApoExMekanismCompatModule() {
        MEK_SUITS_GEM = ApoExItems.ITEMS.register("mek_suits_gem", () -> new MekSuitsGemItem(new Item.Properties().rarity(Rarity.EPIC)));
    }

    @Override
    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(MEK_SUITS_GEM.get());
        }
    }

    @Override
    public void setup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(MekSuitGemDamageHandler.class);
    }
}
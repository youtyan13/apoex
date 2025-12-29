package com.youtyan.apoex.event;

import com.youtyan.apoex.ApoEXMod;
import com.youtyan.apoex.compat.ICreativeTabListener;
import com.youtyan.apoex.item.ApoExItems;
import moffy.addonapi.AddonModule;
import moffy.addonapi.AddonModuleRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ApoEXMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApoExCreativeTabEvents {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ApoExItems.SOCKET_KEY.get());
        }
        for (AddonModule module : AddonModuleRegistry.INSTANCE.getLoadedModules()) {
            if (module instanceof ICreativeTabListener listener) {
                listener.buildContents(event);
            }
        }
    }
}
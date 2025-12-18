package com.youtyan.apoex;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.Path;

@Mod("apoex")
public class ApoEXMod {
    public ApoEXMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
    @SubscribeEvent
    public void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            var modFile = ModList.get().getModFileById("apoex").getFile();
            Path resourcePath = modFile.findResource("apoex_api");
            var packResources = new PathPackResources(
                    "apoex_api",
                    true,
                    resourcePath
            );
            var pack = Pack.readMetaAndCreate(
                    "apoex_api",
                    Component.literal("ApoEX API"),
                    true,
                    (id) -> packResources,
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                event.addRepositorySource((consumer) -> consumer.accept(pack));
            }
        }
    }
}
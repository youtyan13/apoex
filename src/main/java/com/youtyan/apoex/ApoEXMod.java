package com.youtyan.apoex;

import com.mojang.logging.LogUtils;
import com.youtyan.apoex.compat.general.ApoExModuleProvider;
import com.youtyan.apoex.item.ApoExItems;
import com.youtyan.apoex.recipe.ApoExSmithingRecipe;
import com.youtyan.apoex.recipe.ApoExAddSocketRecipe;
import moffy.addonapi.AddonModuleRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod("apoex")
public class ApoEXMod {
    public ApoEXMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        ApoExItems.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        AddonModuleRegistry.INSTANCE.LoadModule(new ApoExModuleProvider(), builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
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
    public static final String MODID = "apoex";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<?>> EX_SOCKET_SMITHING =
            RECIPE_SERIALIZERS.register("ex_socket_smithing", () -> ApoExSmithingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<?>> EX_SOCKET_ADDING =
            RECIPE_SERIALIZERS.register("ex_socket_adding", () -> ApoExAddSocketRecipe.Serializer.INSTANCE);

}
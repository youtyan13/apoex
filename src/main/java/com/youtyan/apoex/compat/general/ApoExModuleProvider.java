package com.youtyan.apoex.compat.general;

import com.youtyan.apoex.compat.mekanism.ApoExMekanismCompatModule;
import moffy.addonapi.AddonModuleProvider;
import com.youtyan.apoex.ApoEXMod;
import net.minecraft.resources.ResourceLocation;

public class ApoExModuleProvider extends AddonModuleProvider {

    @Override
    public void registerRawModules() {
        addRawModule(
                new ResourceLocation(ApoEXMod.MODID, "default_compat"),
                "Default Compat",
                ApoExModule.class,
                new String[] { "apotheosis" },
                true
        );
        addRawModule(
                new ResourceLocation(ApoEXMod.MODID, "mekanism_compat"),
                "Mekanism Compat",
                ApoExMekanismCompatModule.class,
                new String[] { "apotheosis", "mekanism" },
                true
        );
    }

    @Override
    public String getModId() {
        return ApoEXMod.MODID;
    }
}
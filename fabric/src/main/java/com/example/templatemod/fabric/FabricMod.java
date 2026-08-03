package com.example.templatemod.fabric;

import com.example.templatemod.TemplateMod;
import com.example.templatemod.platform.Services;

import net.fabricmc.api.ModInitializer;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Services.setPlatform(new FabricPlatformService());
        FabricContent.register();
        TemplateMod.bootstrap();
    }
}

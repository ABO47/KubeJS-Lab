package com.abo47.kubejslab.fabric;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.platform.Services;

import net.fabricmc.api.ModInitializer;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Services.setPlatform(new FabricPlatformService());
        KubeJSLab.bootstrap();
    }
}

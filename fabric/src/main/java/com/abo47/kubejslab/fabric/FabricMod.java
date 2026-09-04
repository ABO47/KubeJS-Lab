package com.abo47.kubejslab.fabric;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.command.MainCommand;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;


public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Services.setPlatform(new FabricPlatformService());
        FabricContent.register();
        KubeJSLab.bootstrap();
        NetworkRegistry.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> MainCommand.register(dispatcher));
    }
}

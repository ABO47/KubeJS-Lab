package com.abo47.kubejslab.fabric;

import java.nio.file.Path;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.loader.api.FabricLoader;

import com.abo47.kubejslab.platform.PlatformService;

public final class FabricPlatformService implements PlatformService {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public String loaderVersion() {
        return FabricLoader.getInstance().getModContainer("fabricloader")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public void registerNetwork() {
        FabricNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player) {
        FabricNetwork.sendToClient(player);
    }
}

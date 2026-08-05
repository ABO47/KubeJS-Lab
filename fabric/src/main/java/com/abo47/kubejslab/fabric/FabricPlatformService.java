package com.abo47.kubejslab.fabric;

import java.nio.file.Path;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.loader.api.FabricLoader;

import com.abo47.kubejslab.platform.PlatformService;

import io.netty.buffer.Unpooled;

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
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        FabricNetwork.sendToClient(player, serializedHolder, windowId);
    }

    @Override
    public void sendOpenRequest() {
        ClientPlayNetworking.send(FabricNetwork.OPEN_REQUEST, new FriendlyByteBuf(Unpooled.buffer()));
    }
}

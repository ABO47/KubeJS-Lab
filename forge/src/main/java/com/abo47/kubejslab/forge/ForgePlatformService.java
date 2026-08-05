package com.abo47.kubejslab.forge;

import java.nio.file.Path;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import net.minecraftforge.fml.ModList;

import com.abo47.kubejslab.platform.PlatformService;

public final class ForgePlatformService implements PlatformService {
    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String loaderName() {
        return "forge";
    }

    @Override
    public String loaderVersion() {
        return FMLLoader.versionInfo().forgeVersion();
    }

    @Override
    public String modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public void registerNetwork() {
        ForgeNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        int length = serializedHolder.readableBytes();
        byte[] payload = new byte[length];
        serializedHolder.readBytes(payload);
        ForgeNetwork.sendToClient(new ForgeNetwork.OpenScreenPacket(windowId, payload), player);
    }

    @Override
    public void sendOpenRequest() {
        ForgeNetwork.sendToServer(new ForgeNetwork.RequestOpenPacket());
    }
}

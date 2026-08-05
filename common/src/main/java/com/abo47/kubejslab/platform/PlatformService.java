package com.abo47.kubejslab.platform;

import java.nio.file.Path;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public interface PlatformService {
    Path configDir();

    String loaderName();

    String loaderVersion();

    String modVersion(String modId);

    void registerNetwork();

    void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId);

    void sendOpenRequest();
}

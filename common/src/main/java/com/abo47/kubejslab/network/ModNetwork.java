package com.abo47.kubejslab.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.platform.Services;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register() {
        Services.platform().registerNetwork();
    }

    public static void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        Services.platform().sendOpenScreen(player, serializedHolder, windowId);
    }

    public static void requestOpenScreen() {
        Services.platform().sendOpenRequest();
    }
}
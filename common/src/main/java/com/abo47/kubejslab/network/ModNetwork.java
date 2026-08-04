package com.abo47.kubejslab.network;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.platform.Services;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register() {
        Services.platform().registerNetwork();
    }

    public static void sendOpenScreen(ServerPlayer player) {
        Services.platform().sendOpenScreen(player);
    }
}

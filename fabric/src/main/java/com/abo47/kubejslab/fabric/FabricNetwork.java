package com.abo47.kubejslab.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;

public final class FabricNetwork {
    public static final ResourceLocation CHANNEL = new ResourceLocation(KubeJSLab.MOD_ID, "main");

    private static volatile boolean registered;

    private FabricNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
    }

    public static void sendToClient(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}

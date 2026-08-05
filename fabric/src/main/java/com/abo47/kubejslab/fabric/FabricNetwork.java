package com.abo47.kubejslab.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;

import com.abo47.kubejslab.client.ui.LabUIFactory;

import io.netty.buffer.Unpooled;

public final class FabricNetwork {
    public static final ResourceLocation OPEN_SCREEN = new ResourceLocation(KubeJSLab.MOD_ID, "open_screen");
    public static final ResourceLocation OPEN_REQUEST = new ResourceLocation(KubeJSLab.MOD_ID, "open_request");

    private static volatile boolean registered;

    private FabricNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(OPEN_REQUEST, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> LabUIFactory.open(player.blockPosition(), player));
        });
    }

    public static void sendToClient(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(windowId);
        buf.writeVarInt(serializedHolder.readableBytes());
        buf.writeBytes(serializedHolder);
        ServerPlayNetworking.send(player, OPEN_SCREEN, buf);
    }
}
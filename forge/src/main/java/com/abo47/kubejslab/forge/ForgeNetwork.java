package com.abo47.kubejslab.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KubeJSLab.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private static volatile boolean registered;

    private ForgeNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, OpenScreenPacket.class,
                OpenScreenPacket::encode,
                OpenScreenPacket::decode,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        com.abo47.kubejslab.client.ui.LabScreen.open();
                    });
                    ctx.get().setPacketHandled(true);
                });
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static final class OpenScreenPacket {
        static OpenScreenPacket decode(FriendlyByteBuf buf) {
            return new OpenScreenPacket();
        }

        void encode(FriendlyByteBuf buf) {
        }
    }
}

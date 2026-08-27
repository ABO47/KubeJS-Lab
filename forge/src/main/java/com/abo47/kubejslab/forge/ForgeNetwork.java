package com.abo47.kubejslab.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.client.ui.LabClientUIFactory;
import com.abo47.kubejslab.client.ui.LabUIFactory;
import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

import io.netty.buffer.Unpooled;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


public final class ForgeNetwork {
    private static final String PROTOCOL = "3";
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
                        LabClientUIFactory.openFromScreen(new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.payload())), packet.windowId());
                    });
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(1, RequestOpenPacket.class,
                RequestOpenPacket::encode,
                RequestOpenPacket::decode,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            LabUIFactory.open(player.blockPosition(), player);
                        }
                    });
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(2, C2SRecipeEditPacket.class,
                C2SRecipeEditPacket::write,
                C2SRecipeEditPacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            packet.handle(player);
                        }
                    });
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(3, S2CRecipeStatePacket.class,
                S2CRecipeStatePacket::write,
                S2CRecipeStatePacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(packet::handleClient);
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(4, C2SItemEditPacket.class,
                C2SItemEditPacket::write,
                C2SItemEditPacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            packet.handle(player);
                        }
                    });
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(5, S2CItemStatePacket.class,
                S2CItemStatePacket::write,
                S2CItemStatePacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(packet::handleClient);
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(6, C2SBlockEditPacket.class,
                C2SBlockEditPacket::write,
                C2SBlockEditPacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            packet.handle(player);
                        }
                    });
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(7, S2CBlockStatePacket.class,
                S2CBlockStatePacket::write,
                S2CBlockStatePacket::read,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(packet::handleClient);
                    ctx.get().setPacketHandled(true);
                });
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        try {
            CHANNEL.sendToServer(packet);
        } catch (Throwable t) {
            KubeJSLab.LOGGER.error("[Net] Failed to send packet {} to server", packet.getClass().getSimpleName(), t);
            throw t;
        }
    }

    public record OpenScreenPacket(int windowId, byte[] payload) {
        static OpenScreenPacket decode(FriendlyByteBuf buf) {
            int windowId = buf.readVarInt();
            int length = buf.readVarInt();
            byte[] payload = new byte[length];
            buf.readBytes(payload);
            return new OpenScreenPacket(windowId, payload);
        }

        void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(windowId);
            buf.writeVarInt(payload.length);
            buf.writeBytes(payload);
        }
    }

    public record RequestOpenPacket() {
        static RequestOpenPacket decode(FriendlyByteBuf buf) {
            return new RequestOpenPacket();
        }

        void encode(FriendlyByteBuf buf) {
        }
    }
}
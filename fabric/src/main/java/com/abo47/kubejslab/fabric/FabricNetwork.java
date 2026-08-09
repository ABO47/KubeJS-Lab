package com.abo47.kubejslab.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.client.ui.LabUIFactory;
import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


public final class FabricNetwork {
    public static final ResourceLocation OPEN_SCREEN = new ResourceLocation(KubeJSLab.MOD_ID, "open_screen");
    public static final ResourceLocation OPEN_REQUEST = new ResourceLocation(KubeJSLab.MOD_ID, "open_request");
    public static final ResourceLocation RECIPE_EDIT = new ResourceLocation(KubeJSLab.MOD_ID, "recipe_edit");
    public static final ResourceLocation STATE_SYNC = new ResourceLocation(KubeJSLab.MOD_ID, "state_sync");
    public static final ResourceLocation ITEM_EDIT = new ResourceLocation(KubeJSLab.MOD_ID, "item_edit");
    public static final ResourceLocation ITEM_STATE_SYNC = new ResourceLocation(KubeJSLab.MOD_ID, "item_state_sync");

    private static volatile boolean registered;

    private FabricNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(OPEN_REQUEST, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> LabUIFactory.open(player.blockPosition(), player));
        });
        ServerPlayNetworking.registerGlobalReceiver(RECIPE_EDIT, (server, player, handler, buf, responseSender) -> {
            C2SRecipeEditPacket packet = C2SRecipeEditPacket.read(buf);
            server.execute(() -> packet.handle(player));
        });
        ServerPlayNetworking.registerGlobalReceiver(ITEM_EDIT, (server, player, handler, buf, responseSender) -> {
            C2SItemEditPacket packet = C2SItemEditPacket.read(buf);
            server.execute(() -> packet.handle(player));
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
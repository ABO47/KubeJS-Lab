package com.abo47.kubejslab.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
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

    public static void sendRecipeEdit(C2SRecipeEditPacket packet) {
        Services.platform().sendRecipeEdit(packet);
    }

    public static void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        Services.platform().sendRecipeState(player, packet);
    }

    public static void sendItemEdit(C2SItemEditPacket packet) {
        Services.platform().sendItemEdit(packet);
    }

    public static void sendItemState(ServerPlayer player, S2CItemStatePacket packet) {
        Services.platform().sendItemState(player, packet);
    }
}
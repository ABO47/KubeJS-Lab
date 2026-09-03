package com.abo47.kubejslab.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;
import com.abo47.kubejslab.network.loot.C2SLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
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

    public static void sendBlockEdit(C2SBlockEditPacket packet) {
        Services.platform().sendBlockEdit(packet);
    }

    public static void sendBlockState(ServerPlayer player, S2CBlockStatePacket packet) {
        Services.platform().sendBlockState(player, packet);
    }

    public static void sendLootEdit(C2SLootEditPacket packet) {
        Services.platform().sendLootEdit(packet);
    }

    public static void sendLootState(ServerPlayer player, S2CLootStatePacket packet) {
        Services.platform().sendLootState(player, packet);
    }

    public static void sendLootPrefill(C2SLootPrefillPacket packet) {
        Services.platform().sendLootPrefill(packet);
    }

    public static void sendLootPrefill(ServerPlayer player, S2CLootPrefillPacket packet) {
        Services.platform().sendLootPrefill(player, packet);
    }
}
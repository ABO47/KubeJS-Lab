package com.abo47.kubejslab.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

public interface PlatformService {
    void registerNetwork();

    void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId);

    void sendOpenRequest();

    void sendRecipeEdit(C2SRecipeEditPacket packet);

    void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet);
}

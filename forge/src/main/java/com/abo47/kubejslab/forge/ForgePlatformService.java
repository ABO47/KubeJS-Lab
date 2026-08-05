package com.abo47.kubejslab.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

public final class ForgePlatformService implements PlatformService {
    @Override
    public void registerNetwork() {
        ForgeNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        int length = serializedHolder.readableBytes();
        byte[] payload = new byte[length];
        serializedHolder.readBytes(payload);
        ForgeNetwork.sendToClient(new ForgeNetwork.OpenScreenPacket(windowId, payload), player);
    }

    @Override
    public void sendOpenRequest() {
        ForgeNetwork.sendToServer(new ForgeNetwork.RequestOpenPacket());
    }

    @Override
    public void sendRecipeEdit(C2SRecipeEditPacket packet) {
        ForgeNetwork.sendToServer(packet);
    }

    @Override
    public void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        ForgeNetwork.sendToClient(packet, player);
    }
}

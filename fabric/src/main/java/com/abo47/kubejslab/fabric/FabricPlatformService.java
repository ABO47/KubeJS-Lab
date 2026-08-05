package com.abo47.kubejslab.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

import io.netty.buffer.Unpooled;

public final class FabricPlatformService implements PlatformService {
    @Override
    public void registerNetwork() {
        FabricNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        FabricNetwork.sendToClient(player, serializedHolder, windowId);
    }

    @Override
    public void sendOpenRequest() {
        ClientPlayNetworking.send(FabricNetwork.OPEN_REQUEST, new FriendlyByteBuf(Unpooled.buffer()));
    }

    @Override
    public void sendRecipeEdit(C2SRecipeEditPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.RECIPE_EDIT, buf);
    }

    @Override
    public void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.STATE_SYNC, buf);
    }
}

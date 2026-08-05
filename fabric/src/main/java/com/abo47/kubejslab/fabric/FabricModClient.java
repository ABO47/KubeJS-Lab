package com.abo47.kubejslab.fabric;

import com.abo47.kubejslab.client.LabKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.kubejslab.client.ui.LabClientUIFactory;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

import io.netty.buffer.Unpooled;

public final class FabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(LabKeybindings.OPEN_UI);
        ClientTickEvents.END_CLIENT_TICK.register(client -> LabKeybindings.onClientTick());

        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.OPEN_SCREEN, (client, handler, buf, responseSender) -> {
            int windowId = buf.readVarInt();
            int length = buf.readVarInt();
            byte[] payload = new byte[length];
            if (length > 0) {
                buf.readBytes(payload);
            }
            FriendlyByteBuf holder = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));
            client.execute(() -> LabClientUIFactory.openFromScreen(holder, windowId));
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.STATE_SYNC, (client, handler, buf, responseSender) -> {
            S2CRecipeStatePacket packet = S2CRecipeStatePacket.read(buf);
            client.execute(packet::handleClient);
        });
    }
}
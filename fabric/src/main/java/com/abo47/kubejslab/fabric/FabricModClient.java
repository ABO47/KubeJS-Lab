package com.abo47.kubejslab.fabric;

import com.abo47.kubejslab.client.LabKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.abo47.kubejslab.fabric.FabricNetwork;

public final class FabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(LabKeybindings.OPEN_UI);
        ClientTickEvents.END_CLIENT_TICK.register(client -> LabKeybindings.onClientTick());

        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.CHANNEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                com.abo47.kubejslab.client.ui.LabScreen.open();
            });
        });
    }
}

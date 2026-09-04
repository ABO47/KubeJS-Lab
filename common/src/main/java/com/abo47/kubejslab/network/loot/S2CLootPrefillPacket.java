package com.abo47.kubejslab.network.loot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.shell.ScreenSession;
import com.abo47.kubejslab.loot.model.LootFieldValues;


public record S2CLootPrefillPacket(ResourceLocation id, String lootType, LootFieldValues values) {

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id.toString(), 32767);
        buf.writeUtf(lootType, 32767);
        LootFieldValues.write(buf, values);
    }

    public static S2CLootPrefillPacket read(FriendlyByteBuf buf) {
        return new S2CLootPrefillPacket(new ResourceLocation(buf.readUtf()), buf.readUtf(),
                LootFieldValues.read(buf));
    }

    public void handleClient() {
        ScreenSession.applyLootPrefill(id, lootType, values);
    }
}

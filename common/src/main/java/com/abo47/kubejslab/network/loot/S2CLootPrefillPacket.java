package com.abo47.kubejslab.network.loot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.LabScreen;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;


public record S2CLootPrefillPacket(ResourceLocation id, String lootType, LabLootFieldValues values) {

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id.toString(), 32767);
        buf.writeUtf(lootType, 32767);
        LabLootFieldValues.write(buf, values);
    }

    public static S2CLootPrefillPacket read(FriendlyByteBuf buf) {
        return new S2CLootPrefillPacket(new ResourceLocation(buf.readUtf()), buf.readUtf(),
                LabLootFieldValues.read(buf));
    }

    public void handleClient() {
        LabScreen.applyLootPrefill(id, lootType, values);
    }
}

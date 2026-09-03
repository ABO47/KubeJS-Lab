package com.abo47.kubejslab.network.loot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.LabScreen;
import com.abo47.kubejslab.client.ui.loot.LabLootIndex;


public record S2CLootTableListPacket(List<ResourceLocation> tables) {

    public S2CLootTableListPacket {
        tables = tables == null ? List.of() : List.copyOf(tables);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(tables.size());
        for (ResourceLocation id : tables) {
            buf.writeUtf(id.toString(), 32767);
        }
    }

    public static S2CLootTableListPacket read(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 1024);
        List<ResourceLocation> tables = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tables.add(new ResourceLocation(buf.readUtf()));
        }
        return new S2CLootTableListPacket(tables);
    }

    public void handleClient() {
        LabLootIndex.setScannedTables(tables);
        LabScreen.refreshOpen();
    }
}

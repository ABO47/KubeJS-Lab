package com.abo47.kubejslab.network.loot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.runtime.LabLootPrefill;
import com.abo47.kubejslab.network.ModNetwork;


public record C2SLootPrefillPacket(ResourceLocation id, String lootType) {

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id.toString(), 32767);
        buf.writeUtf(lootType, 32767);
    }

    public static C2SLootPrefillPacket read(FriendlyByteBuf buf) {
        return new C2SLootPrefillPacket(new ResourceLocation(buf.readUtf()), buf.readUtf());
    }

    public void handle(ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SLootPrefillPacket rejected: {} lacks permission level 2",
                    player.getName().getString());
            return;
        }
        LabLootFieldValues values = LabLootPrefill.prefill(player.getServer(), id, lootType);
        ModNetwork.sendLootPrefill(player, new S2CLootPrefillPacket(id, lootType, values));
    }
}
